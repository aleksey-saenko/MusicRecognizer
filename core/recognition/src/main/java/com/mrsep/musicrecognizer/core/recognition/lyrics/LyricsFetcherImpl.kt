package com.mrsep.musicrecognizer.core.recognition.lyrics

import android.util.Log
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.core.domain.track.model.Track
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.gildor.coroutines.okhttp.await
import javax.inject.Inject

internal class LyricsFetcherImpl @Inject constructor(
    private val okHttpClient: OkHttpClient,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val json: Json,
) : LyricsFetcher {

    override suspend fun fetch(track: Track): String? {
        return withContext(ioDispatcher) {
            channelFlow {
                val tasks = listOf(
                    async { fetchFromLrcLib(track.title, track.artist) },
                    async { fetchFromLyricHubGenius(track.title, track.artist) },
                )
                tasks.forEach { task -> launch { send(task.await()) } }
            }
                .filterNotNull()
                .firstOrNull()
        }
    }

    private suspend fun fetchFromLrcLib(title: String, artist: String): String? {
        // Don't use optional album_name and duration parameters to expand search
        val requestUrl = "https://lrclib.net/api/get?artist_name=$artist&track_name=$title"
        val request = Request.Builder().url(requestUrl).get().build()
        return fetchByRequest<LrcLibResponseJson>(request) { plainLyrics }
    }

    private suspend fun fetchFromLyricHubGenius(title: String, artist: String): String? {
        val requestUrl = "https://lyrichub.vercel.app/api/genius?query=$title+$artist"
        val request = Request.Builder().url(requestUrl).get().build()
        return fetchByRequest<LyricHubResponseJson>(request) { lyrics }
    }

    private suspend fun fetchFromLyricHubSpotify(title: String, artist: String): String? {
        val requestUrl = "https://lyrichub.vercel.app/api/spotify?query=$title+$artist"
        val request = Request.Builder().url(requestUrl).get().build()
        return fetchByRequest<LyricHubResponseJson>(request) { lyrics }
    }

    private suspend inline fun <reified T> fetchByRequest(
        request: Request,
        providePlainLyrics: T.() -> String?,
    ): String? {
        return try {
            okHttpClient.newCall(request).await().use { response ->
                if (!response.isSuccessful) return null
                json.decodeFromString<T>(response.body!!.string())
                    .providePlainLyrics()?.trim()?.takeIf { it.isNotBlank() }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(this::class.simpleName, "Lyrics fetching is failed (${request.url.host})", e)
            null
        }
    }
}
