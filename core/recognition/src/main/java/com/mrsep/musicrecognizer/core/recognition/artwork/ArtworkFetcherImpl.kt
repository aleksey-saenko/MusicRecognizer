package com.mrsep.musicrecognizer.core.recognition.artwork

import android.util.Log
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.core.domain.track.model.MusicService
import com.mrsep.musicrecognizer.core.domain.track.model.Track
import com.mrsep.musicrecognizer.core.recognition.audd.json.DeezerJson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.gildor.coroutines.okhttp.await
import javax.inject.Inject

internal class ArtworkFetcherImpl @Inject constructor(
    private val okHttpClient: OkHttpClient,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val json: Json,
) : ArtworkFetcher {

    override suspend fun fetchUrl(track: Track): TrackArtwork? {
        return withContext(ioDispatcher) {
            track.trackLinks[MusicService.Deezer]?.run { fetchDeezerSource(this) }
        }
    }

    // https://developers.deezer.com/api/track
    private suspend fun fetchDeezerSource(deezerTrackUrl: String): TrackArtwork? {
        val deezerTrackId = verifyAndParseDeezerTrackId(deezerTrackUrl) ?: return null
        val requestUrl = "https://api.deezer.com/track/$deezerTrackId"
        val request = Request.Builder().url(requestUrl).get().build()
        return try {
            okHttpClient.newCall(request).await().use { response ->
                if (!response.isSuccessful) return null
                val deezerJson = json.decodeFromString<DeezerJson>(response.body!!.string())

                val albumImageUrl = deezerJson.album?.run { coverXl ?: coverBig }
                val albumImageThumbUrl = deezerJson.album?.coverMedium?.takeIf { albumImageUrl != null }
                val albumArtwork = TrackArtwork(url = albumImageUrl, thumbUrl = albumImageThumbUrl)
                if (albumArtwork.url != null && albumArtwork.thumbUrl != null) return@use albumArtwork

                val artistImageUrl = deezerJson.artist?.run { pictureXl ?: pictureBig }
                val artistImageThumb = deezerJson.artist?.pictureMedium?.takeIf { artistImageUrl != null }
                val artistArtwork = TrackArtwork(url = artistImageUrl, thumbUrl = artistImageThumb)
                if (artistArtwork.url != null && artistArtwork.thumbUrl != null) return@use artistArtwork
                if (albumArtwork.url != null) return@use albumArtwork
                if (artistArtwork.url != null) return@use artistArtwork
                null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(this::class.simpleName, "Error during artwork fetching ($requestUrl)", e)
            null
        }
    }

    private fun verifyAndParseDeezerTrackId(deezerTrackUrl: String): String? {
        val pattern = "^https://www\\.deezer\\.com/track/(\\d+)".toRegex()
        val matchResult = pattern.find(deezerTrackUrl)
        return matchResult?.groupValues?.getOrNull(1)
    }
}
