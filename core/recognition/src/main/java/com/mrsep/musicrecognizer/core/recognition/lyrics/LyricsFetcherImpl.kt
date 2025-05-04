package com.mrsep.musicrecognizer.core.recognition.lyrics

import android.util.Log
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.core.domain.track.model.Lyrics
import com.mrsep.musicrecognizer.core.domain.track.model.PlainLyrics
import com.mrsep.musicrecognizer.core.domain.track.model.SyncedLyrics
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

    override suspend fun fetch(track: Track): Lyrics? {
        return withContext(ioDispatcher) {
            channelFlow {
                val tasks = listOf(
                    async { fetchFromLrcLib(track.title, track.artist) },
//                    async { },
                )
                tasks.forEach { task -> launch { send(task.await()) } }
            }
                .filterNotNull()
                .firstOrNull()
        }
    }

    private suspend fun fetchFromLrcLib(title: String, artist: String): Lyrics? {
        // Don't use optional album_name and duration parameters to expand search
        val requestUrl = "https://lrclib.net/api/get?artist_name=$artist&track_name=$title"
        val request = Request.Builder().url(requestUrl).get().build()
        val json = fetchByRequest<LrcLibResponseJson>(request) ?: return null
        return json.syncedLyrics?.takeValidContent()?.parseToSyncedLyrics()
            ?: json.plainLyrics?.takeValidContent()?.run(::PlainLyrics)
    }

    private fun String.takeValidContent() = trim().takeIf { it.isNotBlank() }

    private fun String.parseToSyncedLyrics(): SyncedLyrics? {
        return LyricsConverter().parseFromString(this, parseMetadata = true)
            ?.run { copyAndShiftByOffset(offset) }
            ?.lines?.run(::SyncedLyrics)
    }

    private suspend inline fun <reified T> fetchByRequest(request: Request): T? {
        return try {
            okHttpClient.newCall(request).await().use { response ->
                if (!response.isSuccessful) return null
                json.decodeFromString<T>(response.body!!.string())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(this::class.simpleName, "Lyrics fetching is failed (${request.url.host})", e)
            null
        }
    }
}
