package com.mrsep.musicrecognizer.core.recognition.enhancer.qobuz

import com.mrsep.musicrecognizer.core.common.LocaleProvider
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.core.domain.recognition.model.NetworkError
import com.mrsep.musicrecognizer.core.domain.recognition.model.NetworkResult
import com.mrsep.musicrecognizer.core.domain.track.model.MusicService
import com.mrsep.musicrecognizer.core.domain.track.model.Track
import com.mrsep.musicrecognizer.core.recognition.enhancer.RemoteTrackLinks
import com.mrsep.musicrecognizer.core.recognition.enhancer.TrackLinksFetcher
import com.mrsep.musicrecognizer.core.recognition.enhancer.ScoredTrackCandidate
import com.mrsep.musicrecognizer.core.recognition.enhancer.TrackCandidate
import com.mrsep.musicrecognizer.core.recognition.enhancer.TrackMatcher
import dagger.Lazy
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
import java.util.Locale
import javax.inject.Inject

class QobuzTrackLinksFetcher @Inject constructor(
    private val httpClientLazy: Lazy<HttpClient>,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val localeProvider: LocaleProvider,
) : TrackLinksFetcher {

    override val supportedServices: Set<MusicService> = setOf(MusicService.Qobuz)

    override suspend fun fetch(
        track: Track
    ): NetworkResult<RemoteTrackLinks> = withContext(ioDispatcher) {
        if (MusicService.Qobuz in track.trackLinks) {
            return@withContext NetworkResult.Success(RemoteTrackLinks())
        }

        val queries = buildSearchQueries(track).ifEmpty {
            return@withContext NetworkResult.Success(RemoteTrackLinks())
        }

        var lastError: NetworkError? = null

        val trackMatcher = TrackMatcher(track, enableCache = true)
        var best: ScoredTrackCandidate? = null

        for (query in queries) {
            when (val result = postAutosuggestQuery(query)) {
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
            // Artworks are disabled due to low resolution
            NetworkResult.Success(RemoteTrackLinks(
                artworkThumbUrl = null, // best.candidate.artworkThumbUrl
                artworkUrl = null,      // best.candidate.artworkUrl
                trackLinks = buildMap {
                    best?.candidate?.let { put(it.service, it.trackUrl) }
                }
            ))
        }
    }

    private suspend fun postAutosuggestQuery(
        query: String
    ): NetworkResult<QobuzAutosuggestResponseJson> = withContext(ioDispatcher) {
        // The same track has different Qobuz IDs and regional restrictions in different markets
        val market = resolveQobuzMarket(localeProvider.get())
        val httpClient = httpClientLazy.get()
        val response = try {
            httpClient.get("https://www.qobuz.com/v4/$market/catalog/search/autosuggest") {
                header(HttpHeaders.UserAgent, USER_AGENT)
                parameter("q", query)
                parameter("limit", SEARCH_LIMIT)
                header(HttpHeaders.Accept, "application/json")
                header("X-Requested-With", "XMLHttpRequest")
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
            NetworkResult.Success(response.body<QobuzAutosuggestResponseJson>())
        } catch (e: Exception) {
            ensureActive()
            NetworkError.UnhandledError(message = e.message.orEmpty(), cause = e)
        }
    }

    private fun buildSearchQueries(track: Track): List<String> {
        val isrcQuery = track.isrc
            ?.trim()
            ?.takeIf { it.isNotBlank() }

        val metadataQuery = buildList {
            track.title.trim().takeIf { it.isNotBlank() }?.let(::add)
            track.artist.trim().takeIf { it.isNotBlank() }?.let(::add)
//            track.album?.trim()?.takeIf { it.isNotBlank() }?.let(::add)
        }
            .joinToString(separator = " ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .takeIf { it.isNotBlank() }

        return buildList {
            isrcQuery?.let(::add)
            metadataQuery?.let(::add)
        }.distinct()
    }

    private fun resolveQobuzMarket(locale: Locale): String {
        val country = locale.country.trim().lowercase(Locale.ROOT)
        val normalizedCountry = when {
            country.isBlank() -> DEFAULT_MARKET_COUNTRY
            country in SUPPORTED_LANGUAGE_BY_COUNTRY -> country
            else -> DEFAULT_MARKET_COUNTRY
        }
        // Pair country-language must match the supported values
        val normalizedLanguage = SUPPORTED_LANGUAGE_BY_COUNTRY[normalizedCountry]
        return "$normalizedCountry-$normalizedLanguage"
    }

    private fun QobuzAutosuggestResponseJson.asCandidateSequence() = sequence {
        tracks?.forEach { trackJson -> trackJson?.toCandidate()?.let { yield(it) } }
    }

    private fun QobuzAutosuggestResponseJson.Track.toCandidate(): TrackCandidate? {
        if (id == null) return null
        val titleValue = title?.trim()?.takeIf { it.isNotBlank() } ?: return null
        val artistValue = artist?.trim()?.takeIf { it.isNotBlank() }
        val albumValue = album?.trim()?.takeIf { it.isNotBlank() }
//        // Do we need a link to the album?
//        val trackUrlValue = url?.trim()?.takeIf { it.isNotBlank() }

        return TrackCandidate(
            title = titleValue,
            artist = artistValue,
            album = albumValue,
            duration = null,
            artworkThumbUrl = null,
            artworkUrl = image?.trim()?.takeIf { it.isNotBlank() },
            service = MusicService.Qobuz,
            trackUrl = qobuzTrackUrl(id.toString()),
        )
    }

    private fun qobuzTrackUrl(trackId: String): String = "https://open.qobuz.com/track/$trackId"

    private companion object {
        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:140.0) Gecko/20100101 Firefox/140.0"
        private const val SEARCH_LIMIT = 10

        private const val DEFAULT_MARKET_COUNTRY = "us"

        // https://help.qobuz.com/en/articles/10128-where-is-qobuz-available
        private val SUPPORTED_LANGUAGE_BY_COUNTRY = mapOf(
            "ar" to "es",
            "au" to "en",
            "at" to "de",
            "be" to "fr",
            "br" to "pt",
            "ca" to "en",
            "cl" to "es",
            "co" to "es",
            "dk" to "en",
            "fi" to "en",
            "fr" to "fr",
            "de" to "de",
            "ie" to "en",
            "it" to "it",
            "jp" to "ja",
            "lu" to "fr",
            "nl" to "en",
            "mx" to "es",
            "nz" to "en",
            "no" to "en",
            "pt" to "pt",
            "es" to "es",
            "se" to "en",
            "ch" to "de",
            "gb" to "en",
            "us" to "en",
        )

//        private val SUPPORTED_LANGUAGES = setOf("en", "fr", "de", "it", "ja", "pt", "es")

        private const val DESIRED_ACCEPT_TRACK_SCORE = 0.95
        private const val MIN_ACCEPTED_TRACK_SCORE = 0.75
    }
}
