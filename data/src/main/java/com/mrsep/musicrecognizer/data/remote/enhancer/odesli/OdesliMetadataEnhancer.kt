package com.mrsep.musicrecognizer.data.remote.enhancer.odesli

import android.content.Context
import android.net.Uri
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.data.remote.enhancer.RemoteMetadataEnhancingResultDo
import com.mrsep.musicrecognizer.data.remote.enhancer.TrackMetadataEnhancerDo
import com.mrsep.musicrecognizer.data.track.MusicServiceDo
import com.mrsep.musicrecognizer.data.track.TrackEntity
import com.mrsep.musicrecognizer.data.track.TrackLinkDo
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.gildor.coroutines.okhttp.await
import java.io.IOException
import javax.inject.Inject

class OdesliMetadataEnhancer @Inject constructor(
    moshi: Moshi,
    private val okHttpClient: OkHttpClient,
    @ApplicationContext private val appContext: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : TrackMetadataEnhancerDo {

    @OptIn(ExperimentalStdlibApi::class)
    private val odesliJsonAdapter = moshi.adapter<OdesliResponseJson>()

    @OptIn(ExperimentalStdlibApi::class)
    private val odesliErrorJsonAdapter = moshi.adapter<OdesliErrorResponseJson>()

    private val locale get() = appContext.resources.configuration.locales[0]

    override suspend fun enhance(track: TrackEntity): RemoteMetadataEnhancingResultDo {
        val storedLinks = getUrlsOrderedByPopularity(track.links)
        val queryUrl = storedLinks.firstNotNullOfOrNull { it }
        queryUrl ?: return RemoteMetadataEnhancingResultDo.NoEnhancement
        val hasAllLinks = storedLinks.all { it != null }
        if (hasAllLinks) return RemoteMetadataEnhancingResultDo.NoEnhancement

        return withContext(ioDispatcher) {
            val request = Request.Builder().url(getRequestUrl(queryUrl)).get().build()
            val response = try {
                okHttpClient.newCall(request).await()
            } catch (e: IOException) {
                return@withContext RemoteMetadataEnhancingResultDo.Error.BadConnection
            }
            response.use {
                try {
                    if (response.isSuccessful) {
                        val json = odesliJsonAdapter.fromJson(response.body!!.source())!!
                        val trackLinks = json.toTrackLinks()
                        val artworkUrl = track.links.artwork ?: json.toArtworkUrl()
                        val newLinks = track.links.updateLinks(artworkUrl, trackLinks)
                        RemoteMetadataEnhancingResultDo.Success(track.copy(links = newLinks))
                    } else {
                        val json = odesliErrorJsonAdapter.fromJson(response.body!!.source())
                        RemoteMetadataEnhancingResultDo.Error.HttpError(
                            code = json?.code ?: response.code,
                            message = json?.message ?: response.message
                        )
                    }
                } catch (e: Exception) {
                    RemoteMetadataEnhancingResultDo.Error.UnhandledError(
                        message = e.message ?: "",
                        cause = e
                    )
                }
            }
        }
    }

    private fun getUrlsOrderedByPopularity(links: TrackEntity.Links): List<String?> {
        return with(links) {
            listOf(
                spotify, appleMusic, amazonMusic, youtubeMusic, youtube, deezer, soundCloud,
                yandexMusic, napster, tidal, pandora, musicBrainz, audiomack, audius, boomplay, anghami,
            )
        }
    }

    private fun TrackEntity.Links.updateLinks(
        artworkUrl: String?,
        trackLinks: List<TrackLinkDo>
    ): TrackEntity.Links {
        val linksMap = trackLinks.associate { link -> link.service to link.url }
        return copy(
            artwork = artwork ?: artworkUrl,
            amazonMusic = amazonMusic ?: linksMap[MusicServiceDo.AmazonMusic],
            anghami = anghami ?: linksMap[MusicServiceDo.Anghami],
            appleMusic = appleMusic ?: linksMap[MusicServiceDo.AppleMusic],
            audiomack = audiomack ?: linksMap[MusicServiceDo.Audiomack],
            audius = audius ?: linksMap[MusicServiceDo.Audius],
            boomplay = boomplay ?: linksMap[MusicServiceDo.Boomplay],
            deezer = deezer ?: linksMap[MusicServiceDo.Deezer],
            musicBrainz = musicBrainz ?: linksMap[MusicServiceDo.MusicBrainz],
            napster = napster ?: linksMap[MusicServiceDo.Napster],
            pandora = pandora ?: linksMap[MusicServiceDo.Pandora],
            soundCloud = soundCloud ?: linksMap[MusicServiceDo.Soundcloud],
            spotify = spotify ?: linksMap[MusicServiceDo.Spotify],
            tidal = tidal ?: linksMap[MusicServiceDo.Tidal],
            yandexMusic = yandexMusic ?: linksMap[MusicServiceDo.YandexMusic],
            youtube = youtube ?: linksMap[MusicServiceDo.Youtube],
            youtubeMusic = youtubeMusic ?: linksMap[MusicServiceDo.YoutubeMusic],
        )
    }

    private fun getRequestUrl(queryUrl: String): HttpUrl {
        return HttpUrl.Builder()
            .scheme("https")
            .host("api.song.link")
            .addPathSegment("v1-alpha.1")
            .addPathSegment("links")
            .addQueryParameter("url", Uri.encode(queryUrl))
            .addQueryParameter("userCountry", locale.country)
            .addQueryParameter("songIfSingle", "true")
            .build()
    }
}
