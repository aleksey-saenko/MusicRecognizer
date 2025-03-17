package com.mrsep.musicrecognizer.core.recognition.enhancer.odesli

import android.content.Context
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.core.domain.recognition.TrackMetadataEnhancer
import com.mrsep.musicrecognizer.core.domain.recognition.model.RemoteMetadataEnhancingResult
import com.mrsep.musicrecognizer.core.domain.track.model.MusicService
import com.mrsep.musicrecognizer.core.domain.track.model.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.gildor.coroutines.okhttp.await
import java.io.IOException
import javax.inject.Inject

internal class OdesliMetadataEnhancer @Inject constructor(
    private val okHttpClient: OkHttpClient,
    @ApplicationContext private val appContext: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val json: Json,
) : TrackMetadataEnhancer {

    private val locale get() = appContext.resources.configuration.locales[0]

    override suspend fun enhance(track: Track): RemoteMetadataEnhancingResult {
        val queryUrl = getPriorityLinkForQuery(track.trackLinks)
        queryUrl ?: return RemoteMetadataEnhancingResult.NoEnhancement
        val hasAllLinks = track.trackLinks.size == MusicService.entries.size
        if (hasAllLinks) return RemoteMetadataEnhancingResult.NoEnhancement

        return withContext(ioDispatcher) {
            val request = Request.Builder().url(getRequestUrl(queryUrl)).get().build()
            val response = try {
                okHttpClient.newCall(request).await()
            } catch (e: IOException) {
                return@withContext RemoteMetadataEnhancingResult.Error.BadConnection
            }
            response.use {
                try {
                    if (response.isSuccessful) {
                        val successDto = json.decodeFromString<OdesliResponseJson>(response.body!!.string())
                        val trackLinks = successDto.toTrackLinks()
                        val artworkUrl = track.artworkUrl ?: successDto.toArtworkUrl()
                        val newTrack = track.updateLinks(artworkUrl, trackLinks)
                        RemoteMetadataEnhancingResult.Success(newTrack)
                    } else {
                        val errorDto = json.decodeFromString<OdesliErrorResponseJson>(response.body!!.string())
                        RemoteMetadataEnhancingResult.Error.HttpError(
                            code = errorDto.code ?: response.code,
                            message = errorDto.message ?: response.message
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

    private fun getRequestUrl(queryUrl: String): HttpUrl {
        return HttpUrl.Builder()
            .scheme("https")
            .host("api.song.link")
            .addPathSegment("v1-alpha.1")
            .addPathSegment("links")
            .addQueryParameter("url", queryUrl)
            .addQueryParameter("userCountry", locale.country)
            .addQueryParameter("songIfSingle", "true")
            .build()
    }
}
