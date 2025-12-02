package com.mrsep.musicrecognizer.core.recognition.artwork

import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.core.domain.recognition.model.NetworkError
import com.mrsep.musicrecognizer.core.domain.recognition.model.NetworkResult
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
import kotlinx.io.IOException
import javax.inject.Inject

internal class ArtworkFetcherImpl @Inject constructor(
    private val httpClientLazy: dagger.Lazy<HttpClient>,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ArtworkFetcher {

    override suspend fun fetchUrl(track: Track): NetworkResult<TrackArtwork?> {
        return withContext(ioDispatcher) {
            track.trackLinks[MusicService.Deezer]?.run { fetchDeezerSource(this) }
                ?: NetworkResult.Success(null)
        }
    }

    // https://developers.deezer.com/api/track
    private suspend fun fetchDeezerSource(deezerTrackUrl: String): NetworkResult<TrackArtwork?> {
        val deezerTrackId = verifyAndParseDeezerTrackId(deezerTrackUrl)?: return NetworkResult.Success(null)
        val response = try {
            val httpClient = httpClientLazy.get()
            httpClient.get("https://api.deezer.com/track") {
                url {
                    appendPathSegments(deezerTrackId)
                }
            }
        } catch (e: IOException) {
            return NetworkError.BadConnection(e.message)
        }
        return if (response.status.isSuccess()) {
            try {
                val json = response.body<DeezerJson>()
                val albumImageUrl = json.album?.run { coverXl ?: coverBig }
                val albumImageThumbUrl = json.album?.coverMedium?.takeIf { albumImageUrl != null }
                val albumArtwork = TrackArtwork(url = albumImageUrl, thumbUrl = albumImageThumbUrl)

                val artistImageUrl = json.artist?.run { pictureXl ?: pictureBig }
                val artistImageThumb = json.artist?.pictureMedium?.takeIf { artistImageUrl != null }
                val artistArtwork = TrackArtwork(url = artistImageUrl, thumbUrl = artistImageThumb)

                val result = albumArtwork.takeIf { it.url != null && it.thumbUrl != null }
                    ?: artistArtwork.takeIf { it.url != null && it.thumbUrl != null }
                    ?: albumArtwork.takeIf { it.url != null }
                    ?: artistArtwork.takeIf { it.url != null }
                NetworkResult.Success(result)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                NetworkError.UnhandledError(message = e.message ?: "", cause = e)
            }
        } else {
            NetworkError.HttpError(
                code = response.status.value,
                message = response.status.description
            )
        }
    }

    private fun verifyAndParseDeezerTrackId(deezerTrackUrl: String): String? {
        val pattern = "^https://www\\.deezer\\.com/track/(\\d+)".toRegex()
        val matchResult = pattern.find(deezerTrackUrl)
        return matchResult?.groupValues?.getOrNull(1)
    }
}
