package com.mrsep.musicrecognizer.core.recognition.enhancer.odesli

import android.content.Context
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.core.domain.recognition.TrackMetadataEnhancer
import com.mrsep.musicrecognizer.core.domain.recognition.model.RemoteMetadataEnhancingResult
import com.mrsep.musicrecognizer.core.domain.track.model.MusicService
import com.mrsep.musicrecognizer.core.domain.track.model.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

internal class OdesliMetadataEnhancer @Inject constructor(
    private val httpClientLazy: dagger.Lazy<HttpClient>,
    @ApplicationContext private val appContext: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : TrackMetadataEnhancer {

    private val locale get() = appContext.resources.configuration.locales[0]

    override suspend fun enhance(track: Track): RemoteMetadataEnhancingResult {
        val queryUrl = getPriorityLinkForQuery(track.trackLinks)
        queryUrl ?: return RemoteMetadataEnhancingResult.NoEnhancement
        val hasAllLinks = track.trackLinks.size == MusicService.entries.size
        if (hasAllLinks) return RemoteMetadataEnhancingResult.NoEnhancement

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
            } catch (_: IOException) {
                return@withContext RemoteMetadataEnhancingResult.Error.BadConnection
            }
            try {
                if (response.status.isSuccess()) {
                    val successDto: OdesliResponseJson = response.body()
                    val trackLinks = successDto.toTrackLinks()
                    val artworkUrl = track.artworkUrl ?: successDto.toArtworkUrl()
                    val newTrack = track.updateLinks(artworkUrl, trackLinks)
                    RemoteMetadataEnhancingResult.Success(newTrack)
                } else {
                    val errorDto: OdesliErrorResponseJson = response.body()
                    RemoteMetadataEnhancingResult.Error.HttpError(
                        code = errorDto.code ?: response.status.value,
                        message = errorDto.message ?: response.status.description
                    )
                }
            } catch (e: Exception) {
                RemoteMetadataEnhancingResult.Error.UnhandledError(
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

    private fun Track.updateLinks(
        newArtworkUrl: String?,
        newTrackLinks: Map<MusicService, String>
    ): Track = copy(
        artworkUrl = artworkUrl ?: newArtworkUrl,
        trackLinks = trackLinks + (newTrackLinks - trackLinks.keys),
    )
}
