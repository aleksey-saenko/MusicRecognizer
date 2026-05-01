package com.mrsep.musicrecognizer.core.metadata.tracklink.musicbrainz

import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.core.domain.recognition.model.NetworkError
import com.mrsep.musicrecognizer.core.domain.recognition.model.NetworkResult
import com.mrsep.musicrecognizer.core.domain.track.model.MusicService
import com.mrsep.musicrecognizer.core.domain.track.model.Track
import com.mrsep.musicrecognizer.core.metadata.tracklink.RemoteTrackLinks
import com.mrsep.musicrecognizer.core.metadata.tracklink.TrackCandidate
import com.mrsep.musicrecognizer.core.metadata.tracklink.TrackLinksFetcher
import com.mrsep.musicrecognizer.core.metadata.tracklink.TrackLinksSource
import com.mrsep.musicrecognizer.core.metadata.tracklink.TrackMatcher
import com.mrsep.musicrecognizer.core.metadata.tracklink.TrackMatcherOptions
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

// https://musicbrainz.org/doc/MusicBrainz_API#isrc
// https://musicbrainz.org/doc/MusicBrainz_API/Search#Recording

class MusicBrainzTrackLinksFetcher @Inject constructor(
    private val httpClientLazy: dagger.Lazy<HttpClient>,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : TrackLinksFetcher {

    override val source = TrackLinksSource.MusicBrainz
    override val supportedServices: Set<MusicService> = setOf(MusicService.MusicBrainz)

    override suspend fun fetch(
        track: Track
    ): NetworkResult<RemoteTrackLinks> = withContext(ioDispatcher) {
        val trackIsrc = track.isrc

        val response = if (trackIsrc.isNullOrBlank()) {
            searchRecordingsByQuery(buildSearchQuery(track))
        } else {
            searchRecordingsByIsrc(trackIsrc)
        }

        when (response) {
            is NetworkResult.Success -> {
                val trackMatcher = TrackMatcher(
                    track = track,
                    options = TrackMatcherOptions(considerDuration = track.duration != null)
                )
                val best = trackMatcher.firstDesiredOrBestAcceptableOf(
                    candidates = response.data.asCandidateSequence().take(SEARCH_LIMIT),
                    minAcceptScore = MIN_ACCEPTED_TRACK_SCORE,
                    desiredAcceptScore = DESIRED_ACCEPT_TRACK_SCORE,
                )
                NetworkResult.Success(RemoteTrackLinks(
                    trackLinks = buildMap { best?.candidate?.let { put(it.service, it.trackUrl) } }
                ))
            }

            is NetworkError -> response
        }
    }

    private suspend fun searchRecordingsByIsrc(
        isrc: String
    ): NetworkResult<MusicBrainzRecordingSearchJson> = withContext(ioDispatcher) {
        val httpClient = httpClientLazy.get()
        val response = try {
            httpClient.get("$MUSICBRAINZ_API_ROOT/isrc/$isrc") {
                header(HttpHeaders.Accept, RESPONSE_ACCEPT_HEADER)
                parameter("fmt", RESPONSE_FORMAT)
                parameter("inc", "artist-credits+isrcs")
                // no search limit for Non-MBID Lookups
            }
        } catch (e: IOException) {
            return@withContext NetworkError.BadConnection(e.message)
        } catch (e: Exception) {
            ensureActive()
            return@withContext NetworkError.UnhandledError(message = e.message.orEmpty(), cause = e)
        }

        if (!response.status.isSuccess()) {
            return@withContext NetworkError.HttpError(
                code = response.status.value,
                message = response.status.description
            )
        }

        try {
            NetworkResult.Success(response.body<MusicBrainzRecordingSearchJson>())
        } catch (e: Exception) {
            ensureActive()
            NetworkError.UnhandledError(message = e.message.orEmpty(), cause = e)
        }
    }

    private suspend fun searchRecordingsByQuery(
        query: String
    ): NetworkResult<MusicBrainzRecordingSearchJson> = withContext(ioDispatcher) {
        val httpClient = httpClientLazy.get()
        val response = try {
            httpClient.get("$MUSICBRAINZ_API_ROOT/recording") {
                header(HttpHeaders.Accept, RESPONSE_ACCEPT_HEADER)
                parameter("fmt", RESPONSE_FORMAT)
                parameter("limit", SEARCH_LIMIT)
                parameter("query", query)
            }
        } catch (e: IOException) {
            return@withContext NetworkError.BadConnection(e.message)
        } catch (e: Exception) {
            ensureActive()
            return@withContext NetworkError.UnhandledError(message = e.message.orEmpty(), cause = e)
        }

        if (!response.status.isSuccess()) {
            return@withContext NetworkError.HttpError(
                code = response.status.value,
                message = response.status.description
            )
        }

        try {
            NetworkResult.Success(response.body<MusicBrainzRecordingSearchJson>())
        } catch (e: Exception) {
            ensureActive()
            NetworkError.UnhandledError(message = e.message.orEmpty(), cause = e)
        }
    }

    private fun buildSearchQuery(track: Track): String = buildString {
        append("""recording:"${track.title.trim()}" AND artist:"${track.artist.trim()}"""")
        track.album?.trim()?.takeIf { it.isNotBlank() }?.let { album ->
            append(""" AND release:"$album"""")
        }
        track.releaseDate?.year?.toString()?.let { year ->
            append(""" AND date:"$year"""")
        }
    }

    private fun MusicBrainzRecordingSearchJson.asCandidateSequence() = sequence {
        recordings?.forEach { recording ->
            recording?.toCandidate()?.let { yield(it) }
        }
    }

    private fun MusicBrainzRecordingJson.toCandidate(): TrackCandidate? {
        val recordingId = id?.trim().takeIf { !it.isNullOrBlank() } ?: return null
        val titleValue = title?.trim().takeIf { !it.isNullOrBlank() } ?: return null
        val artistValue = artistCredit.joinedArtists() ?: return null
        val durationValue = length?.milliseconds

        return TrackCandidate(
            title = titleValue,
            artist = artistValue,
            album = null,
            duration = durationValue,
            artworkThumbUrl = null,
            artworkUrl = null,
            service = MusicService.MusicBrainz,
            trackUrl = musicBrainzRecordingUrl(recordingId),
        )
    }

    private fun List<MusicBrainzRecordingJson.ArtistCredit?>?.joinedArtists(): String? {
        return orEmpty()
            .mapNotNull { credit ->
                val name = credit?.name?.trim()?.takeIf { it.isNotBlank() }
                    ?: credit?.artist?.name?.trim()?.takeIf { it.isNotBlank() }

                if (name == null) return@mapNotNull null
                name + credit?.joinphrase.orEmpty()
            }
            .joinToString(separator = "")
            .trim()
            .takeIf { it.isNotBlank() }
    }

    private fun musicBrainzRecordingUrl(recordingId: String): String {
        return "$MUSICBRAINZ_WEB_ROOT/recording/$recordingId"
    }

    private companion object {
        private const val MUSICBRAINZ_API_ROOT = "https://musicbrainz.org/ws/2"
        private const val MUSICBRAINZ_WEB_ROOT = "https://musicbrainz.org"

        private const val RESPONSE_ACCEPT_HEADER = "application/json"
        private const val RESPONSE_FORMAT = "json"
        private const val SEARCH_LIMIT = 10

        private const val DESIRED_ACCEPT_TRACK_SCORE = 0.95
        private const val MIN_ACCEPTED_TRACK_SCORE = 0.80
    }
}
