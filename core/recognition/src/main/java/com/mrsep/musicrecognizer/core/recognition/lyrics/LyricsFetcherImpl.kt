package com.mrsep.musicrecognizer.core.recognition.lyrics

import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.core.domain.recognition.model.NetworkError
import com.mrsep.musicrecognizer.core.domain.recognition.model.NetworkResult
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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import javax.inject.Inject

internal class LyricsFetcherImpl @Inject constructor(
    private val httpClientLazy: dagger.Lazy<HttpClient>,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : LyricsFetcher {

    override suspend fun fetch(track: Track): NetworkResult<Lyrics?> {
        return withContext(ioDispatcher) {
            channelFlow {
                val tasks = listOf(
                    async { fetchFromLrcLib(track) },
//                    async { },
                )
                tasks.forEach { task -> launch { send(task.await()) } }
            }
                .transformWhile { result ->
                    emit(result)
                    (result as? NetworkResult.Success)?.data == null
                }
                .firstOrNull()
                ?: NetworkError.BadConnection()
        }
    }

    private suspend fun fetchFromLrcLib(track: Track): NetworkResult<Lyrics?> {
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
        return fetchByRequest<LrcLibResponseJson>(request) { json ->
            json.syncedLyrics?.takeValidContent()?.parseToSyncedLyrics()
                ?: json.plainLyrics?.takeValidContent()?.run(::PlainLyrics)
        }
    }

    private fun String.takeValidContent() = trim().takeIf { it.isNotBlank() }

    private fun String.parseToSyncedLyrics(): SyncedLyrics? {
        return LyricsConverter().parseFromString(this, parseMetadata = true)
            ?.run { copyAndShiftByOffset(offset) }
            ?.lines?.run(::SyncedLyrics)
    }

    private suspend inline fun <reified T> fetchByRequest(
        builder: HttpRequestBuilder,
        transform: (T) -> Lyrics?
    ): NetworkResult<Lyrics?> {
        val response = try {
            val httpClient = httpClientLazy.get()
            httpClient.request(builder)
        } catch (e: IOException) {
            return NetworkError.BadConnection(e.message)
        }
        return if (response.status.isSuccess()) {
            try {
                NetworkResult.Success(transform(response.body<T>()))
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
}
