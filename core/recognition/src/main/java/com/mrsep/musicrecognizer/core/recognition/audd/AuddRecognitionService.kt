package com.mrsep.musicrecognizer.core.recognition.audd

import android.util.Log
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.core.domain.preferences.AuddConfig
import com.mrsep.musicrecognizer.core.domain.recognition.AudioRecording
import com.mrsep.musicrecognizer.core.domain.recognition.RemoteRecognitionService
import com.mrsep.musicrecognizer.core.domain.recognition.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.core.recognition.audd.json.AuddResponseJson
import com.mrsep.musicrecognizer.core.recognition.audd.json.toRecognitionResult
import com.mrsep.musicrecognizer.core.recognition.audd.websocket.WebSocketSession
import com.mrsep.musicrecognizer.core.recognition.audd.websocket.isRecoverableWebSocketFailure
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
import io.ktor.http.takeFrom
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import java.io.IOException
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private const val TAG = "AuddRecognitionService"

internal class AuddRecognitionService @AssistedInject constructor(
    @Assisted private val config: AuddConfig,
    private val webSocketSession: WebSocketSession,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val httpClientLazy: dagger.Lazy<HttpClient>,
    private val json: Json,
) : RemoteRecognitionService {

    override suspend fun recognize(recording: AudioRecording): RemoteRecognitionResult {
        return recognize(recording.data, recording.startTimestamp, recording.duration)
    }

    private suspend fun recognize(
        recording: ByteArray,
        startTimestamp: Instant,
        recordingDuration: Duration,
    ): RemoteRecognitionResult = withContext(ioDispatcher) {
        val httpClient = httpClientLazy.get()
        val response = try {
            httpClient.submitFormWithBinaryData(
                url = REST_BASE_URL,
                formData = formData {
                    append("api_token", config.apiToken)
                    append("return", RETURN_PARAM)
                    append(
                        key = "file",
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
            try {
                response.body<AuddResponseJson>()
                    .toRecognitionResult(json, startTimestamp, recordingDuration)
            } catch (e: Exception) {
                RemoteRecognitionResult.Error.UnhandledError(message = e.message ?: "", cause = e)
            }
        } else {
            RemoteRecognitionResult.Error.HttpError(
                code = response.status.value,
                message = response.status.description
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun recognizeFirst(recordingFlow: Flow<AudioRecording>) = withContext(ioDispatcher) {
        val recordingChannel = Channel<AudioRecording>(Channel.UNLIMITED)
        var emptyRecordingStream = false
        val recordWithTimeout = launch {
            recordingFlow
                .onEmpty { emptyRecordingStream = true }
                .collect(recordingChannel::send)
            recordingChannel.close()
            if (!emptyRecordingStream) {
                delay(TIMEOUT_AFTER_RECORDING_FINISHED)
            }
        }

        // Initially bad connection
        // for case when after TIMEOUT_AFTER_RECORDING_FINISHED there is no any response yet
        var lastResult: RemoteRecognitionResult = RemoteRecognitionResult.Error.BadConnection
        val remoteResult = async {
            var unprocessedRecording: AudioRecording? = null
            webSocketSession.startReconnectingSession(WEB_SOCKET_RECONNECT_LIMIT) {
                url {
                    takeFrom(WEB_SOCKET_BASE_URL)
                    parameters.apply {
                        append("api_token", config.apiToken)
                        append("return", RETURN_PARAM)
                    }
                }
            }.mapLatest { wsConnectionResult ->
                val wsConnection = wsConnectionResult.getOrElse { cause ->
                    val httpClient = httpClientLazy.get()
                    lastResult = if (isRecoverableWebSocketFailure(cause, httpClient.engine)) {
                        RemoteRecognitionResult.Error.BadConnection
                    } else {
                        RemoteRecognitionResult.Error.UnhandledError(cause = cause)
                    }
                    return@mapLatest lastResult
                }
                do {
                    val recordingToSend = unprocessedRecording
                        ?: recordingChannel.receiveCatching().getOrNull()
                        ?: break // the channel is closed, no more recordings
                    unprocessedRecording = recordingToSend
                    // We don't expect any responses right now, but make sure the channel is empty
                    while (wsConnection.responseChannel.tryReceive().isSuccess) {
                        Log.w(TAG, "Unexpected response dropped")
                    }
                    try {
                        wsConnection.sendRecording(recordingToSend.data)
                    } catch (_: ClosedSendChannelException) {
                        awaitCancellation()
                    }
                    lastResult = try {
                        withTimeout(WEB_SOCKET_RESPONSE_TIMEOUT) {
                            wsConnection.responseChannel.receive().toRecognitionResult(
                                json, recordingToSend.startTimestamp, recordingToSend.duration
                            )
                        }
                    } catch (_: TimeoutCancellationException) {
                        RemoteRecognitionResult.Error.BadConnection
                    } catch (_: ClosedReceiveChannelException) {
                        awaitCancellation()
                    }
                    unprocessedRecording = null
                } while (lastResult == RemoteRecognitionResult.NoMatches)
                lastResult
            }.first()
        }

        select {
            remoteResult.onJoin { recordWithTimeout.cancelAndJoin() }
            recordWithTimeout.onJoin {
                remoteResult.cancelAndJoin()
                if (emptyRecordingStream) {
                    lastResult = RemoteRecognitionResult.Error.BadRecording(
                        "Empty audio recording stream"
                    )
                }
            }
        }
        lastResult
    }

    @AssistedFactory
    interface Factory {
        fun create(config: AuddConfig): AuddRecognitionService
    }

    companion object {
        private const val REST_BASE_URL = "https://api.audd.io/"
        private const val WEB_SOCKET_BASE_URL = "wss://api.audd.io/ws/"
        private const val RETURN_PARAM = "lyrics,spotify,apple_music,deezer,napster,musicbrainz"
        private const val RECORDING_MEDIA_TYPE = "audio/mp4"

        private val TIMEOUT_AFTER_RECORDING_FINISHED = 10.seconds
        private val WEB_SOCKET_RESPONSE_TIMEOUT = 15.seconds
        private const val WEB_SOCKET_RECONNECT_LIMIT = 1
    }
}
