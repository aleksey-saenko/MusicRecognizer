package com.mrsep.musicrecognizer.core.recognition.enhancer.odesli

import android.content.Context
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.core.domain.recognition.model.NetworkError
import com.mrsep.musicrecognizer.core.domain.recognition.model.NetworkResult
import com.mrsep.musicrecognizer.core.domain.track.model.MusicService
import com.mrsep.musicrecognizer.core.domain.track.model.Track
import com.mrsep.musicrecognizer.core.recognition.enhancer.RemoteTrackLinks
import com.mrsep.musicrecognizer.core.recognition.enhancer.TrackLinksFetcher
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

internal class OdesliTrackLinksFetcher @Inject constructor(
    private val httpClientLazy: dagger.Lazy<HttpClient>,
    @ApplicationContext private val appContext: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : TrackLinksFetcher {

    private val locale get() = appContext.resources.configuration.locales[0]

    override suspend fun fetch(track: Track): NetworkResult<RemoteTrackLinks> {
        val queryUrl = getPriorityLinkForQuery(track.trackLinks)
        queryUrl ?: return NetworkResult.Success(RemoteTrackLinks())
        val hasAllLinks = track.trackLinks.size == MusicService.entries.size
        if (hasAllLinks) return NetworkResult.Success(RemoteTrackLinks())

        return withContext(ioDispatcher) {
            val httpClient = httpClientLazy.get()
            val response = try {
                httpClient.get("https://api.song.link/v1-alpha.1/links") {
                    url {
                        parameters.append("url", queryUrl)
                        parameters.append("userCountry", locale.country)
                        parameters.append("songIfSingle", "true")
                    }
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
