package com.mrsep.musicrecognizer.core.recognition.artwork

import android.util.Log
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.core.domain.track.model.MusicService
import com.mrsep.musicrecognizer.core.domain.track.model.Track
import com.mrsep.musicrecognizer.core.recognition.audd.json.DeezerJson
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.appendPathSegments
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class ArtworkFetcherImpl @Inject constructor(
    private val httpClientLazy: dagger.Lazy<HttpClient>,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ArtworkFetcher {

    override suspend fun fetchUrl(track: Track): TrackArtwork? {
        return withContext(ioDispatcher) {
            track.trackLinks[MusicService.Deezer]?.run { fetchDeezerSource(this) }
        }
    }

    // https://developers.deezer.com/api/track
    private suspend fun fetchDeezerSource(deezerTrackUrl: String): TrackArtwork? {
        val deezerTrackId = verifyAndParseDeezerTrackId(deezerTrackUrl) ?: return null
        val deezerResponse = try {
            val httpClient = httpClientLazy.get()
            val response = httpClient.get("https://api.deezer.com/track") {
                url {
                    appendPathSegments(deezerTrackId)
                }
            }
            if (!response.status.isSuccess()) return null
            response.body<DeezerJson>()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(this::class.simpleName, "Error during artwork fetching", e)
            return null
        }
        val albumImageUrl = deezerResponse.album?.run { coverXl ?: coverBig }
        val albumImageThumbUrl = deezerResponse.album?.coverMedium?.takeIf { albumImageUrl != null }
        val albumArtwork = TrackArtwork(url = albumImageUrl, thumbUrl = albumImageThumbUrl)

        val artistImageUrl = deezerResponse.artist?.run { pictureXl ?: pictureBig }
        val artistImageThumb = deezerResponse.artist?.pictureMedium?.takeIf { artistImageUrl != null }
        val artistArtwork = TrackArtwork(url = artistImageUrl, thumbUrl = artistImageThumb)

        return albumArtwork.takeIf { it.url != null && it.thumbUrl != null }
            ?: artistArtwork.takeIf { it.url != null && it.thumbUrl != null }
            ?: albumArtwork.takeIf { it.url != null }
            ?: artistArtwork.takeIf { it.url != null }
    }

    private fun verifyAndParseDeezerTrackId(deezerTrackUrl: String): String? {
        val pattern = "^https://www\\.deezer\\.com/track/(\\d+)".toRegex()
        val matchResult = pattern.find(deezerTrackUrl)
        return matchResult?.groupValues?.getOrNull(1)
    }
}
