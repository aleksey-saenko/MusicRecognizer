package com.mrsep.musicrecognizer.core.recognition.lyrics

import android.util.Log
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.core.domain.track.model.Lyrics
import com.mrsep.musicrecognizer.core.domain.track.model.PlainLyrics
import com.mrsep.musicrecognizer.core.domain.track.model.SyncedLyrics
import com.mrsep.musicrecognizer.core.domain.track.model.Track
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import io.ktor.http.HttpMethod
import io.ktor.http.isSuccess
import io.ktor.http.takeFrom
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class LyricsFetcherImpl @Inject constructor(
    private val httpClientLazy: dagger.Lazy<HttpClient>,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : LyricsFetcher {

    override suspend fun fetch(track: Track): Lyrics? {
        return withContext(ioDispatcher) {
            channelFlow {
                val tasks = listOf(
                    async { fetchFromLrcLib(track) },
//                    async { },
                )
                tasks.forEach { task -> launch { send(task.await()) } }
            }
                .filterNotNull()
                .firstOrNull()
        }
    }

    private suspend fun fetchFromLrcLib(track: Track): Lyrics? {
        val request = HttpRequestBuilder().apply {
            method = HttpMethod.Get
            url {
                takeFrom("https://lrclib.net/api/get")
                parameters.append("artist_name", track.artist)
                parameters.append("track_name", track.title)
                track.duration?.inWholeSeconds?.toString()?.let { duration ->
                    parameters.append("duration", duration)
                }
            }
        }
        val json = fetchByRequest<LrcLibResponseJson>(request)
        return json?.syncedLyrics?.takeValidContent()?.parseToSyncedLyrics()
            ?: json?.plainLyrics?.takeValidContent()?.run(::PlainLyrics)
    }

    private fun String.takeValidContent() = trim().takeIf { it.isNotBlank() }

    private fun String.parseToSyncedLyrics(): SyncedLyrics? {
        return LyricsConverter().parseFromString(this, parseMetadata = true)
            ?.run { copyAndShiftByOffset(offset) }
            ?.lines?.run(::SyncedLyrics)
    }

    private suspend inline fun <reified T> fetchByRequest(builder: HttpRequestBuilder): T? {
        return try {
            val httpClient = httpClientLazy.get()
            val response = httpClient.request(builder)
            if (!response.status.isSuccess()) return null
            response.body<T>()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(this::class.simpleName, "Lyrics fetching is failed (${builder.url.host})", e)
            null
        }
    }
}
