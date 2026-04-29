package com.mrsep.musicrecognizer.core.metadata.tracklink.odesli

import com.mrsep.musicrecognizer.core.common.LocaleProvider
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.core.domain.recognition.model.NetworkError
import com.mrsep.musicrecognizer.core.domain.recognition.model.NetworkResult
import com.mrsep.musicrecognizer.core.domain.track.model.MusicService
import com.mrsep.musicrecognizer.core.domain.track.model.Track
import com.mrsep.musicrecognizer.core.metadata.tracklink.RemoteTrackLinks
import com.mrsep.musicrecognizer.core.metadata.tracklink.TrackLinksFetcher
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class OdesliTrackLinksFetcher @Inject constructor(
    private val httpClientLazy: dagger.Lazy<HttpClient>,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val localeProvider: LocaleProvider,
) : TrackLinksFetcher {

    override val supportedServices = setOf(
        MusicService.AmazonMusic,
        MusicService.Anghami,
        MusicService.AppleMusic,
        MusicService.Audiomack,
        MusicService.Audius,
        MusicService.Boomplay,
        MusicService.Deezer,
        MusicService.Napster,
        MusicService.Pandora,
        MusicService.Soundcloud,
        MusicService.Spotify,
        MusicService.Tidal,
        MusicService.YandexMusic,
        MusicService.Youtube,
        MusicService.YoutubeMusic,
    )

    override suspend fun fetch(track: Track): NetworkResult<RemoteTrackLinks> {
        val queryUrl = getPriorityLinkForQuery(track.trackLinks)
        queryUrl ?: return NetworkResult.Success(RemoteTrackLinks())
        val hasAllLinks = supportedServices.all(track.trackLinks::contains)
        if (hasAllLinks) return NetworkResult.Success(RemoteTrackLinks())

        val country = localeProvider.get().country.ifEmpty { "us" }
        return withContext(ioDispatcher) {
            val httpClient = httpClientLazy.get()
            val response = try {
                httpClient.get("https://api.song.link/v1-alpha.1/links") {
                    parameter("url", queryUrl)
                    parameter("userCountry", country)
                    parameter("songIfSingle", "true")
                }
            } catch (e: IOException) {
                return@withContext NetworkError.BadConnection(e.message)
            }
            try {
                if (response.status.isSuccess()) {
                    val successDto: OdesliResponseJson = response.body()
                    val trackLinks = successDto.toTrackLinks()
                    val artworkUrl = track.artworkUrl ?: successDto.toArtworkUrl()
                    NetworkResult.Success(RemoteTrackLinks(
                        artworkThumbUrl = null,
                        artworkUrl = artworkUrl,
                        trackLinks = trackLinks
                    ))
                } else {
                    val errorDto: OdesliErrorResponseJson = response.body()
                    NetworkError.HttpError(
                        code = errorDto.code ?: response.status.value,
                        message = errorDto.message ?: response.status.description
                    )
                }
            } catch (e: Exception) {
                ensureActive()
                NetworkError.UnhandledError(
                    message = e.message ?: "",
                    cause = e
                )
            }
        }
    }

    private fun getPriorityLinkForQuery(links: Map<MusicService, String>): String? {
        return links[MusicService.Spotify]
            ?: links[MusicService.AppleMusic]
            ?: links[MusicService.AmazonMusic]
            ?: links[MusicService.YoutubeMusic]
            ?: links[MusicService.Youtube]
            ?: links[MusicService.Deezer]
            ?: links[MusicService.Soundcloud]
            ?: links[MusicService.YandexMusic]
            ?: links[MusicService.Napster]
            ?: links[MusicService.Tidal]
            ?: links[MusicService.Pandora]
            ?: links[MusicService.MusicBrainz]
            ?: links[MusicService.Audiomack]
            ?: links[MusicService.Audius]
            ?: links[MusicService.Boomplay]
            ?: links[MusicService.Anghami]
    }
}
