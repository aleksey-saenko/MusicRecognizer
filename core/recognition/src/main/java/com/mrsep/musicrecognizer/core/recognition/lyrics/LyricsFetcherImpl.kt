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
                    // Disabled due to https://github.com/asrvd/lyrist/issues/9
//                    async { fetchLyristSource(track.title, track.artist) },
                    // Disabled due to inconsistent line breaks in lyrics
//                    async { fetchOvhSource(track.title, track.artist) },
                    async { fetchLrcLibSource(track.title, track.artist) },
                )
                tasks.forEach { task -> launch { send(task.await()) } }
            }
                .filterNotNull()
                .firstOrNull()
        }
    }

    private suspend fun fetchLyristSource(title: String, artist: String): String? {
        val requestUrl = "https://lyrist.vercel.app/api/:$artist/:$title"
        val request = Request.Builder().url(requestUrl).get().build()
        return try {
            okHttpClient.newCall(request).await().use { response ->
                if (!response.isSuccessful) return null
                json.decodeFromString<LyristResponseJson>(response.body!!.string())
                    .lyrics?.trim()?.takeIf { it.isNotBlank() }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(this::class.simpleName, "Lyrics fetching is failed ($requestUrl)", e)
            null
        }
    }

    private suspend fun fetchOvhSource(title: String, artist: String): String? {
        val requestUrl = "https://api.lyrics.ovh/v1/$artist/$title"
        val request = Request.Builder().url(requestUrl).get().build()
        return try {
            okHttpClient.newCall(request).await().use { response ->
                if (!response.isSuccessful) return null
                json.decodeFromString<LyricsOvhResponseJson>(response.body!!.string()).lyrics?.run {
                    val lineBreak = indexOf("\n")
                    if (startsWith("Paroles de la chanson") && lineBreak != -1) {
                        drop(lineBreak + 1)
                    } else {
                        this
                    }.trim().takeIf { it.isNotBlank() }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(this::class.simpleName, "Lyrics fetching is failed ($requestUrl)", e)
            null
        }
    }

    private suspend fun fetchLrcLibSource(title: String, artist: String): String? {
        // Don't use optional album_name and duration parameters to expand search
        val requestUrl = "https://lrclib.net/api/get?artist_name=$artist&track_name=$title"
        val request = Request.Builder().url(requestUrl).get().build()
        return try {
            okHttpClient.newCall(request).await().use { response ->
                if (!response.isSuccessful) return null
                json.decodeFromString<LrcLibResponseJson>(response.body!!.string())
                    .plainLyrics?.trim()?.takeIf { it.isNotBlank() }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(this::class.simpleName, "Lyrics fetching is failed ($requestUrl)", e)
            null
        }
    }
}
