package com.mrsep.musicrecognizer.core.recognition.acrcloud

import android.util.Log
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.core.domain.preferences.AcrCloudConfig
import com.mrsep.musicrecognizer.core.domain.recognition.AudioRecording
import com.mrsep.musicrecognizer.core.domain.recognition.RemoteRecognitionService
import com.mrsep.musicrecognizer.core.domain.recognition.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.core.recognition.acrcloud.json.AcrCloudResponseJson
import com.mrsep.musicrecognizer.core.recognition.acrcloud.json.toRecognitionResult
import com.mrsep.musicrecognizer.core.recognition.artwork.ArtworkFetcher
import com.mrsep.musicrecognizer.core.recognition.lyrics.LyricsFetcher
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import java.time.Instant
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private const val TAG = "AcrCloudRecognitionService"

internal class AcrCloudRecognitionService @AssistedInject constructor(
    @Assisted private val config: AcrCloudConfig,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val httpClientLazy: dagger.Lazy<HttpClient>,
    private val artworkFetcher: ArtworkFetcher,
    private val lyricsFetcher: LyricsFetcher,
) : RemoteRecognitionService {

    override suspend fun recognize(recording: AudioRecording): RemoteRecognitionResult {
        return recognize(recording.data, recording.startTimestamp, recording.duration)
    }

    private suspend fun recognize(
        recording: ByteArray,
        recordingStartTimestamp: Instant,
        recordingDuration: Duration,
    ): RemoteRecognitionResult = withContext(ioDispatcher) {
        if (recordingDuration > RECORDING_DURATION_LIMIT) {
            // It's strange, but tests show that recordings are truncated from the beginning, not the end
            Log.w(TAG, "It is discouraged to send recordings longer than $RECORDING_DURATION_LIMIT;" +
                        " the beginning will be truncated on the server side.")
        }
        val httpClient = httpClientLazy.get()
        val timestamp = Instant.now().epochSecond.toString()
        val signature = createSignature(timestamp, config)

        val response = try {
            httpClient.submitFormWithBinaryData(
                url = "https://${config.host}$ENDPOINT",
                formData = formData {
                    append("timestamp", timestamp)
                    append("access_key", config.accessKey)
                    append("signature_version", SIGNATURE_VERSION)
                    append("signature", signature)
                    append("data_type", DATA_TYPE)
                    append("sample_bytes", "${recording.size}")
                    append(
                        key = "sample",
                        value = recording,
                        headers = Headers.build {
                            append(HttpHeaders.ContentDisposition, "filename=sample")
                            append(HttpHeaders.ContentType, RECORDING_MEDIA_TYPE)
                        }
                    )
                }
            )
        } catch (_: IOException) {
            return@withContext RemoteRecognitionResult.Error.BadConnection
        }

        if (response.status.isSuccess()) {
            val remoteResult = try {
                response.body<AcrCloudResponseJson>()
                    .toRecognitionResult(recordingStartTimestamp, recordingDuration)
            } catch (e: Exception) {
                RemoteRecognitionResult.Error.UnhandledError(message = e.message ?: "", cause = e)
            }
            if (remoteResult is RemoteRecognitionResult.Success) {
                val track = remoteResult.track
                val artworkDeferred = async { artworkFetcher.fetchUrl(track) }
                val lyricsDeferred = async { lyricsFetcher.fetch(track) }
                val finalTrack = track.copy(
                    lyrics = lyricsDeferred.await(),
                    artworkThumbUrl = artworkDeferred.await()?.thumbUrl,
                    artworkUrl = artworkDeferred.await()?.url,
                )
                RemoteRecognitionResult.Success(finalTrack)
            } else {
                remoteResult
            }
        } else {
            RemoteRecognitionResult.Error.HttpError(
                code = response.status.value,
                message = response.status.description
            )
        }
    }

    override suspend fun recognizeFirst(
        recordingFlow: Flow<AudioRecording>,
    ): RemoteRecognitionResult = withContext(ioDispatcher) {
        var retryCounter = 0
        // initially bad connection
        // for case when after TIMEOUT_AFTER_RECORDING_FINISHED there is no any response yet
        var lastResult: RemoteRecognitionResult =
            RemoteRecognitionResult.Error.BadConnection

        val recordingChannel = Channel<AudioRecording>(Channel.UNLIMITED)
        val recordWithTimeoutJob = launch {
            recordingFlow.collect(recordingChannel::send)
            recordingChannel.close()
            delay(TIMEOUT_AFTER_RECORDING_FINISHED)
        }

        val remoteResultJob = recordingChannel.receiveAsFlow()
            .onEmpty {
                lastResult = RemoteRecognitionResult.Error.BadRecording(
                    "Empty audio recording stream"
                )
            }
            .transformWhile<AudioRecording, Unit> { recording ->
                lastResult = recognize(recording)
                while (lastResult.isRetryRequired() && retryCounter < RETRY_ON_ERROR_LIMIT) {
                    retryCounter++
                    lastResult = recognize(recording)
                }
                // continue recognition only if no matches
                lastResult is RemoteRecognitionResult.NoMatches
            }
            .launchIn(this)

        select {
            remoteResultJob.onJoin { recordWithTimeoutJob.cancelAndJoin() }
            recordWithTimeoutJob.onJoin { remoteResultJob.cancelAndJoin() }
        }

        lastResult
    }

    private fun RemoteRecognitionResult.isRetryRequired() = when (this) {
        RemoteRecognitionResult.Error.BadConnection -> true
        else -> false
    }

    private fun createSignature(timestamp: String, config: AcrCloudConfig): String {
        val sigString = METHOD + "\n" + ENDPOINT + "\n" + config.accessKey +
                "\n" + DATA_TYPE + "\n" + SIGNATURE_VERSION + "\n" + timestamp
        return encryptByHMACSHA1(sigString.toByteArray(), config.accessSecret.toByteArray())
    }

    @Suppress("SpellCheckingInspection")
    private fun encryptByHMACSHA1(data: ByteArray, key: ByteArray): String {
        return try {
            val signingKey = SecretKeySpec(key, "HmacSHA1")
            val mac = Mac.getInstance("HmacSHA1")
            mac.init(signingKey)
            val rawHmac = mac.doFinal(data)
            Base64.Default.encode(rawHmac)
        } catch (e: Exception) {
            Log.e(this::class.simpleName, "Error during signature encryption", e)
            ""
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(config: AcrCloudConfig): AcrCloudRecognitionService
    }

    companion object {
        private const val METHOD = "POST"
        private const val ENDPOINT = "/v1/identify"
        private const val DATA_TYPE = "audio"
        private const val SIGNATURE_VERSION = "1"

        val RECORDING_DURATION_LIMIT = 12.seconds
        private const val RECORDING_MEDIA_TYPE = "audio/mpeg"
        private const val RETRY_ON_ERROR_LIMIT = 1
        private val TIMEOUT_AFTER_RECORDING_FINISHED = 10.seconds
    }
}
