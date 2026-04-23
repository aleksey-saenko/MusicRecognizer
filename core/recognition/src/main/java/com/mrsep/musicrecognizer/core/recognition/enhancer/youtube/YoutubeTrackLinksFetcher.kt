package com.mrsep.musicrecognizer.core.recognition.enhancer.youtube

import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.core.domain.recognition.model.NetworkError
import com.mrsep.musicrecognizer.core.domain.recognition.model.NetworkResult
import com.mrsep.musicrecognizer.core.domain.track.model.MusicService
import com.mrsep.musicrecognizer.core.domain.track.model.Track
import com.mrsep.musicrecognizer.core.recognition.enhancer.RemoteTrackLinks
import com.mrsep.musicrecognizer.core.recognition.enhancer.TrackLinksFetcher
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import javax.inject.Inject

// Inspired by https://github.com/TeamNewPipe/NewPipeExtractor v0.26.1

class YoutubeTrackLinksFetcher @Inject constructor(
    private val httpClientLazy: dagger.Lazy<HttpClient>,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : TrackLinksFetcher {

    override val supportedServices = setOf(
        MusicService.Youtube,
        MusicService.YoutubeMusic,
    )

    override suspend fun fetch(track: Track): NetworkResult<RemoteTrackLinks> {
        val query = buildSearchQuery(track)
        if (query.isBlank()) return NetworkResult.Success(RemoteTrackLinks())

        val shouldSearchYoutube = MusicService.Youtube !in track.trackLinks
        val shouldSearchYoutubeMusic = MusicService.YoutubeMusic !in track.trackLinks

        return when {
            !shouldSearchYoutube && !shouldSearchYoutubeMusic -> NetworkResult.Success(RemoteTrackLinks())

            shouldSearchYoutube && shouldSearchYoutubeMusic -> coroutineScope {
                val youtubeDeferred = async { searchYouTube(query, track) }
                val youtubeMusicDeferred = async { searchYouTubeMusicSongsThenVideos(query, track) }

                val youtubeResult = youtubeDeferred.await()
                val youtubeMusicResult = youtubeMusicDeferred.await()

                val trackLinks: MutableMap<MusicService, String> = mutableMapOf()
                var artworkThumbUrl: String? = null
                var artworkUrl: String? = null

                if (youtubeMusicResult is NetworkResult.Success) {
                    trackLinks.putAll(youtubeMusicResult.data.trackLinks)
                    if (youtubeMusicResult.data.artworkUrl != null) {
                        artworkUrl = youtubeMusicResult.data.artworkUrl
                        artworkThumbUrl = youtubeMusicResult.data.artworkThumbUrl
                    }
                }

                if (youtubeResult is NetworkResult.Success) {
                    trackLinks.putAll(youtubeResult.data.trackLinks)
                    if (youtubeResult.data.artworkUrl != null && artworkUrl == null) {
                        artworkUrl = youtubeResult.data.artworkUrl
                        artworkThumbUrl = youtubeResult.data.artworkThumbUrl
                    }
                }

                when {
                    trackLinks.isNotEmpty() -> NetworkResult.Success(
                        RemoteTrackLinks(
                            artworkThumbUrl = artworkThumbUrl,
                            artworkUrl = artworkUrl,
                            trackLinks = trackLinks,
                        )
                    )

                    youtubeResult !is NetworkResult.Success -> youtubeResult
                    youtubeMusicResult !is NetworkResult.Success -> youtubeMusicResult

                    else -> NetworkResult.Success(
                        RemoteTrackLinks(
                            artworkThumbUrl = artworkThumbUrl,
                            artworkUrl = artworkUrl,
                            trackLinks = emptyMap(),
                        )
                    )
                }
            }

            shouldSearchYoutube -> searchYouTube(query, track)
            else -> searchYouTubeMusicSongsThenVideos(query, track)
        }
    }

    private suspend fun searchYouTube(
        query: String,
        track: Track
    ): NetworkResult<RemoteTrackLinks> = withContext(ioDispatcher) {
        val response = try {
            val httpClient = httpClientLazy.get()
            httpClient.post(YOUTUBE_SEARCH_URL) {
                header(HttpHeaders.UserAgent, USER_AGENT_WEB)
                header(HttpHeaders.Accept, "application/json, text/plain")
                header(HttpHeaders.AcceptLanguage, "$DEFAULT_HL, en;q=0.9")
                header(HttpHeaders.Cookie, "SOCS=CAE=")
                header(HttpHeaders.Origin, "https://www.youtube.com")
                header(HttpHeaders.Referrer, "https://www.youtube.com")
                header("X-YouTube-Client-Name", YOUTUBE_CLIENT_NAME_ID)
                header("X-YouTube-Client-Version", YOUTUBE_CLIENT_VERSION)
                contentType(ContentType.Application.Json)
                setBody(
                    YoutubeSearchRequestJson(
                        context = YoutubeSearchRequestJson.ContextJson(
                            client = YoutubeSearchRequestJson.ClientJson(
                                clientName = YOUTUBE_CLIENT_NAME,
                                clientVersion = YOUTUBE_CLIENT_VERSION,
                                hl = DEFAULT_HL,
                                gl = DEFAULT_GL,
                                originalUrl = YOUTUBE_ORIGINAL_URL,
                                platform = DEFAULT_PLATFORM,
                                utcOffsetMinutes = DEFAULT_UTC_OFFSET_MINUTES,
                            ),
                            request = YoutubeSearchRequestJson.RequestJson(
                                internalExperimentFlags = emptyList(),
                                useSsl = true,
                            ),
                            user = YoutubeSearchRequestJson.UserJson(
                                lockedSafetyMode = false,
                            ),
                        ),
                        query = query,
                        params = YOUTUBE_VIDEOS_PARAMS,
                    )
                )
            }
        } catch (e: IOException) {
            return@withContext NetworkError.BadConnection(e.message)
        }
        return@withContext try {
            if (response.status.isSuccess()) {
                val body = response.body<YoutubeSearchResponseJson>()
                val trackMatcher = TrackMatcher(track)
                val best = trackMatcher.firstDesiredOrBestAcceptableOf(
                    candidates = body.asCandidateSequence().take(10),
                    minAcceptScore = MIN_ACCEPTED_TRACK_SCORE,
                    desiredAcceptScore = DESIRED_ACCEPT_TRACK_SCORE
                )
                // Artworks are disabled due to undesirable widescreen aspect ratio
                NetworkResult.Success(RemoteTrackLinks(
                    artworkThumbUrl = null, // best.candidate.artworkThumbUrl
                    artworkUrl = null,      // best.candidate.artworkThumbUrl
                    trackLinks = buildMap {
                        best?.candidate?.let { put(it.service, it.trackUrl) }
                    }
                ))
            } else {
                NetworkError.HttpError(
                    code = response.status.value,
                    message = response.status.description
                        .ifBlank { "Failed to parse YouTube search response" }
                )
            }
        } catch (e: Exception) {
            ensureActive()
            NetworkError.UnhandledError(message = e.message ?: "", cause = e)
        }
    }

    private suspend fun searchYouTubeMusicSongsThenVideos(
        query: String,
        track: Track
    ): NetworkResult<RemoteTrackLinks> = withContext(ioDispatcher) {
        var lastError: NetworkError? = null

        val trackMatcher = TrackMatcher(track, enableCache = true)
        var best: ScoredTrackCandidate? = null

        val filterParams = listOf(YOUTUBE_MUSIC_SONGS_PARAMS, YOUTUBE_MUSIC_VIDEOS_PARAMS)

        for (params in filterParams) {
            when (val result = postYouTubeMusic(query, params)) {
                is NetworkResult.Success -> {
                    val current = trackMatcher.firstDesiredOrBestAcceptableOf(
                        candidates = result.data.asCandidateSequence().take(10),
                        minAcceptScore = MIN_ACCEPTED_TRACK_SCORE,
                        desiredAcceptScore = DESIRED_ACCEPT_TRACK_SCORE,
                    ) ?: continue

                    if (current.score > (best?.score ?: 0.0)) {
                        best = current
                        if (best.score >= DESIRED_ACCEPT_TRACK_SCORE) break
                    }
                }

                is NetworkError -> lastError = result
            }
        }

        if (lastError != null && best == null) {
            lastError
        } else {
            // Artworks are disabled due to undesirable widescreen aspect ratio
            NetworkResult.Success(RemoteTrackLinks(
                artworkThumbUrl = null, // best.candidate.artworkThumbUrl
                artworkUrl = null,      // best.candidate.artworkUrl
                trackLinks = buildMap {
                    best?.candidate?.let { put(it.service, it.trackUrl) }
                }
            ))
        }
    }

    private suspend fun postYouTubeMusic(
        query: String,
        params: String
    ): NetworkResult<YoutubeMusicSearchResponseJson> = withContext(ioDispatcher) {
        val response = try {
            val httpClient = httpClientLazy.get()
            httpClient.post(YOUTUBE_MUSIC_SEARCH_URL) {
                header(HttpHeaders.UserAgent, USER_AGENT_WEB)
                header(HttpHeaders.Accept, "application/json, text/plain")
                header(HttpHeaders.AcceptLanguage, "$DEFAULT_HL, en;q=0.9")
                header(HttpHeaders.Origin, "https://music.youtube.com")
                header(HttpHeaders.Referrer, "https://music.youtube.com")
                header("X-YouTube-Client-Name", YOUTUBE_MUSIC_CLIENT_NAME_ID)
                header("X-YouTube-Client-Version", YOUTUBE_MUSIC_CLIENT_VERSION)
                contentType(ContentType.Application.Json)
                setBody(
                    YoutubeSearchRequestJson(
                        context = YoutubeSearchRequestJson.ContextJson(
                            client = YoutubeSearchRequestJson.ClientJson(
                                clientName = YOUTUBE_MUSIC_CLIENT_NAME,
                                clientVersion = YOUTUBE_MUSIC_CLIENT_VERSION,
                                hl = DEFAULT_HL,
                                gl = DEFAULT_GL,
                                originalUrl = null,
                                platform = DEFAULT_PLATFORM,
                                utcOffsetMinutes = DEFAULT_UTC_OFFSET_MINUTES,
                            ),
                            request = YoutubeSearchRequestJson.RequestJson(
                                internalExperimentFlags = emptyList(),
                                useSsl = true,
                            ),
                            user = YoutubeSearchRequestJson.UserJson(
                                lockedSafetyMode = false,
                            ),
                        ),
                        query = query,
                        params = params
                    )
                )
            }
        } catch (e: IOException) {
            return@withContext NetworkError.BadConnection(e.message)
        }
        return@withContext try {
            if (response.status.isSuccess()) {
                NetworkResult.Success(response.body<YoutubeMusicSearchResponseJson>())
            } else {
                NetworkError.HttpError(
                    code = response.status.value,
                    message = response.status.description
                        .ifBlank { "Failed to parse YouTube Music search response" }
                )
            }
        } catch (e: Exception) {
            ensureActive()
            NetworkError.UnhandledError(message = e.message ?: "", cause = e)
        }
    }

    private fun buildSearchQuery(track: Track): String {
        return buildList {
            track.title.trim().takeIf { it.isNotBlank() }?.let(::add)
            track.artist.trim().takeIf { it.isNotBlank() }?.let(::add)
//            track.album?.trim()?.takeIf { it.isNotBlank() }?.let(::add)
        }
            .joinToString(separator = " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun YoutubeSearchResponseJson.asCandidateSequence(): Sequence<TrackCandidate> = sequence {
        contents?.twoColumnSearchResultsRenderer?.primaryContents?.sectionListRenderer?.contents.orEmpty()
            .forEach { section ->
                section?.itemSectionRenderer?.contents.orEmpty().forEach { item ->
                    item?.videoRenderer?.toCandidate()?.let { yield(it) }
                }
            }
    }

    private fun YoutubeMusicSearchResponseJson.asCandidateSequence(): Sequence<TrackCandidate> = sequence {
        contents?.tabbedSearchResultsRenderer?.tabs.orEmpty().forEach { tab ->
            tab?.tabRenderer?.content?.sectionListRenderer?.contents.orEmpty().forEach { section ->
                section?.musicShelfRenderer?.contents.orEmpty().forEach { item ->
                    item?.musicResponsiveListItemRenderer?.toCandidate()?.let { yield(it) }
                }
            }
        }
    }

    private fun YoutubeSearchResponseJson.VideoRendererJson.toCandidate(): TrackCandidate? {
        val videoIdValue = videoId?.trim().takeIf { !it.isNullOrBlank() } ?: return null
        return TrackCandidate(
            service = MusicService.Youtube,
            title = textValue(title) ?: return null,
            artist = textValue(longBylineText) ?: return null,
            album = null,
            duration = parseDuration(textValue(lengthText)),
            trackUrl = youtubeWatchUrl(videoIdValue),
            artworkThumbUrl = thumbnail?.thumbnails?.firstOrNull()?.url?.trim(),
            artworkUrl = thumbnail?.thumbnails?.lastOrNull()?.url?.trim(),
        )
    }

    private fun YoutubeMusicSearchResponseJson.MusicResponsiveListItemRendererJson.toCandidate(): TrackCandidate? {
        if (musicItemRendererDisplayPolicy == "MUSIC_ITEM_RENDERER_DISPLAY_POLICY_GREY_OUT") {
            return null
        }

        val videoIdValue = playlistItemData?.videoId?.trim().takeIf { !it.isNullOrBlank() }
            ?: overlay
                ?.musicItemThumbnailOverlayRenderer
                ?.content
                ?.musicPlayButtonRenderer
                ?.playNavigationEndpoint
                ?.watchEndpoint
                ?.videoId
                ?.trim().takeIf { !it.isNullOrBlank() }

        if (videoIdValue.isNullOrBlank()) return null

        val titleLine = flexColumns.orEmpty()
            .getOrNull(0)
            ?.musicResponsiveListItemFlexColumnRenderer
            ?.text?.let(::textValue) ?: return null

        val metadataLine = flexColumns.orEmpty()
            .getOrNull(1)
            ?.musicResponsiveListItemFlexColumnRenderer
            ?.text?.let(::textValue) ?: return null

        val parsedMetadata = parseMusicMetadata(metadataLine)

        return TrackCandidate(
            service = MusicService.YoutubeMusic,
            title = titleLine,
            artist = parsedMetadata.artist ?: return null,
            album = parsedMetadata.album,
            duration = parsedMetadata.duration,
            trackUrl = youtubeMusicWatchUrl(videoIdValue),
            artworkThumbUrl = thumbnail
                ?.musicThumbnailRenderer
                ?.thumbnail
                ?.thumbnails
                ?.firstOrNull()
                ?.url
                ?.trim(),
            artworkUrl = thumbnail
                ?.musicThumbnailRenderer
                ?.thumbnail
                ?.thumbnails
                ?.lastOrNull()
                ?.url
                ?.trim(),
        )
    }

    private fun parseMusicMetadata(raw: String?): ParsedMusicMetadata {
        val normalized = raw
            ?.replace(UNICODE_NO_BREAK_SPACE, " ")
            ?.replace(Regex("\\s+"), " ")
            ?.trim()
            .orEmpty()

        if (normalized.isBlank()) return ParsedMusicMetadata()

        val tokens = normalized
            .split(BULLET_SEPARATOR)
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (tokens.isEmpty()) return ParsedMusicMetadata()

        val duration = tokens.lastOrNull()?.let(::parseDuration)
        val tokensWithoutDuration = if (duration != null) tokens.dropLast(1) else tokens

        val artist = when (tokensWithoutDuration.size) {
            0 -> null
            1 -> tokensWithoutDuration[0]
            else -> tokensWithoutDuration.dropLast(1).joinToString(", ")
        }

        val album = when {
            tokensWithoutDuration.size >= 2 -> tokensWithoutDuration.last()
            else -> null
        }

        return ParsedMusicMetadata(
            artist = artist?.takeIf { it.isNotBlank() },
            album = album?.takeIf { it.isNotBlank() },
            duration = duration,
        )
    }

    private fun parseDuration(raw: String?): Duration? {
        val text = raw?.trim()?.takeIf { it.isNotBlank() } ?: return null

        val colonMatch = COLON_DURATION_REGEX.matchEntire(text)
        if (colonMatch != null) {
            val parts = text.split(":").mapNotNull { it.toLongOrNull() }
            return when (parts.size) {
                2 -> parts[0].minutes + parts[1].seconds
                3 -> parts[0].hours + parts[1].minutes + parts[2].seconds
                else -> null
            }
        }

        val hours = HOURS_REGEX.find(text)?.groupValues?.getOrNull(1)?.toLongOrNull() ?: 0L
        val minutes = MINUTES_REGEX.find(text)?.groupValues?.getOrNull(1)?.toLongOrNull() ?: 0L
        val seconds = SECONDS_REGEX.find(text)?.groupValues?.getOrNull(1)?.toLongOrNull() ?: 0L

        return if (hours == 0L && minutes == 0L && seconds == 0L) {
            null
        } else {
            hours.hours + minutes.minutes + seconds.seconds
        }
    }

    private fun textValue(block: TextBlockJson?): String? {
        val simpleText = block?.simpleText?.trim()?.takeIf { it.isNotBlank() }
        if (simpleText != null) return simpleText

        val runText = block?.runs
            ?.mapNotNull { it?.text }
            ?.joinToString(separator = "")
            ?.trim()
            ?.takeIf { it.isNotBlank() }

        if (runText != null) return runText

        return block?.accessibility?.accessibilityData?.label?.trim()?.takeIf { it.isNotBlank() }
    }

    private fun youtubeWatchUrl(videoId: String): String = "https://www.youtube.com/watch?v=$videoId"

    private fun youtubeMusicWatchUrl(videoId: String): String = "https://music.youtube.com/watch?v=$videoId"

    private companion object {

        private const val YOUTUBE_SEARCH_URL =
            "https://www.youtube.com/youtubei/v1/search?prettyPrint=false"
        private const val YOUTUBE_MUSIC_SEARCH_URL =
            "https://music.youtube.com/youtubei/v1/search?prettyPrint=false"

        private const val USER_AGENT_WEB = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:140.0) Gecko/20100101 Firefox/140.0"

        private const val YOUTUBE_CLIENT_NAME = "WEB"
        private const val YOUTUBE_CLIENT_NAME_ID = "1"
        private const val YOUTUBE_CLIENT_VERSION = "2.20260414.01.00"
        private const val YOUTUBE_ORIGINAL_URL = "https://www.youtube.com"

        private const val YOUTUBE_MUSIC_CLIENT_NAME = "WEB_REMIX"
        private const val YOUTUBE_MUSIC_CLIENT_NAME_ID = "67"
        private const val YOUTUBE_MUSIC_CLIENT_VERSION = "1.20260121.03.00"

        private const val DEFAULT_HL = "en-GB"
        private const val DEFAULT_GL = "GB"
        private const val DEFAULT_PLATFORM = "DESKTOP"
        private const val DEFAULT_UTC_OFFSET_MINUTES = 0

        // NewPipeExtractor params for YoutubeSearchQueryHandlerFactory.VIDEOS
        @Suppress("SpellCheckingInspection")
        private const val YOUTUBE_VIDEOS_PARAMS = "EgIQAfABAQ%3D%3D"

        // NewPipeExtractor params for YoutubeSearchQueryHandlerFactory.MUSIC_SONGS
        @Suppress("SpellCheckingInspection")
        private const val YOUTUBE_MUSIC_SONGS_PARAMS = "Eg-KAQwIARAAGAAgACgAMABqChAEEAUQAxAKEAk%3D"
        // NewPipeExtractor params for YoutubeSearchQueryHandlerFactory.MUSIC_VIDEOS
        @Suppress("SpellCheckingInspection")
        private const val YOUTUBE_MUSIC_VIDEOS_PARAMS = "Eg-KAQwIABABGAAgACgAMABqChAEEAUQAxAKEAk%3D"

        private val COLON_DURATION_REGEX = Regex("""^\d+:\d{2}(:\d{2})?$""")
        private val HOURS_REGEX = Regex("""(\d+)\s+hours?""", RegexOption.IGNORE_CASE)
        private val MINUTES_REGEX = Regex("""(\d+)\s+minutes?""", RegexOption.IGNORE_CASE)
        private val SECONDS_REGEX = Regex("""(\d+)\s+seconds?""", RegexOption.IGNORE_CASE)

        private const val BULLET_SEPARATOR = " • "
        private const val UNICODE_NO_BREAK_SPACE = "\u00A0"

        private const val DESIRED_ACCEPT_TRACK_SCORE = 0.95
        private const val MIN_ACCEPTED_TRACK_SCORE = 0.70
    }
}

private data class ParsedMusicMetadata(
    val artist: String? = null,
    val album: String? = null,
    val duration: Duration? = null,
)
