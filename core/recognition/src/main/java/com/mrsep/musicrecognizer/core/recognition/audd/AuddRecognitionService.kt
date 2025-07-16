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
import com.mrsep.musicrecognizer.core.recognition.audd.websocket.SocketEvent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.WebSocket
import okhttp3.coroutines.executeAsync
import okio.ByteString.Companion.toByteString
import java.io.IOException
import java.io.InterruptedIOException
import java.net.ProtocolException
import java.net.SocketTimeoutException
import java.security.cert.CertificateException
import java.time.Instant
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private const val TAG = "AuddRecognitionService"

internal class AuddRecognitionService @AssistedInject constructor(
    @Assisted private val config: AuddConfig,
    private val webSocketSession: WebSocketSession,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val okHttpClient: dagger.Lazy<OkHttpClient>,
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
        val multipartBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("api_token", config.apiToken)
            .addFormDataPart("return", RETURN_PARAM)
            .addFormDataPart(
                "file",
                "sample",
                recording.toRequestBody(RECORDING_MEDIA_TYPE.toMediaTypeOrNull())
            )
            .build()
        val request = Request.Builder()
            .url(REST_BASE_URL)
            .post(multipartBody)
            .build()

        val response = try {
            okHttpClient.get().newCall(request).executeAsync()
        } catch (e: IOException) {
            return@withContext RemoteRecognitionResult.Error.BadConnection
        }
        response.use {
            if (response.isSuccessful) {
                try {
                    json.decodeFromString<AuddResponseJson>(response.body.string())
                        .toRecognitionResult(json, startTimestamp, recordingDuration)
                } catch (e: Exception) {
                    RemoteRecognitionResult.Error.UnhandledError(message = e.message ?: "", cause = e)
                }
            } else {
                RemoteRecognitionResult.Error.HttpError(
                    code = response.code,
                    message = response.message
                )
            }
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

        val currentWebSocket = MutableStateFlow<Result<WebSocket>?>(null)
        var failedConnectionCount = 0
        val wsUrl = WEB_SOCKET_TEMPLATE_URL.format(RETURN_PARAM, config.apiToken)
        val wsResponseChannel = webSocketSession.startReconnectingSession(wsUrl)
            .transformWhile { socketEvent ->
                when (socketEvent) {
                    // Web socket session will be restarted; but we don't expect these events
                    is SocketEvent.ConnectionClosing -> {
                        currentWebSocket.update { null }
                    }
                    is SocketEvent.ConnectionClosed -> {
                        currentWebSocket.update { null }
                        Log.w(TAG, "Web socket session was closed unexpectedly: " +
                                "code=${socketEvent.shutdownReason.code}, " +
                                "reason=${socketEvent.shutdownReason.reason}")
                    }

                    is SocketEvent.ConnectionFailed -> {
                        failedConnectionCount++
                        val newSocketResult = if (
                            isRecoverableWebSocketFailure(socketEvent.throwable) &&
                                failedConnectionCount < WEB_SOCKET_RECONNECT_LIMIT + 1
                        ) {
                            null
                        } else {
                            Result.failure<WebSocket>(socketEvent.throwable)
                        }
                        currentWebSocket.update { newSocketResult }
                    }

                    is SocketEvent.ConnectionOpened -> {
                        currentWebSocket.update { Result.success(socketEvent.webSocket) }
                    }

                    is SocketEvent.MessageReceived -> try {
                        emit(json.decodeFromString<AuddResponseJson>(socketEvent.message))
                    } catch (e: IllegalArgumentException) {
                        currentWebSocket.update { Result.failure(e) }
                    }
                }
                currentWebSocket.value?.isFailure != true
            }
            .buffer(Channel.UNLIMITED)
            .produceIn(this)

        // Initially bad connection
        // for case when after TIMEOUT_AFTER_RECORDING_FINISHED there is no any response yet
        var lastResult: RemoteRecognitionResult = RemoteRecognitionResult.Error.BadConnection
        var unprocessedRecording: AudioRecording? = null
        val remoteResult = async {
            currentWebSocket.mapLatest {  webSocketResult ->
                // Wait while webSocket is opening
                if (webSocketResult == null) awaitCancellation()
                webSocketResult.onSuccess { webSocket ->
                    do {
                        val localRecording = unprocessedRecording
                            ?: recordingChannel.receiveCatching().getOrNull()
                            ?: break // the channel is closed, no more recordings
                        // We don't expect any responses right now, but make sure the channel is empty
                        while (wsResponseChannel.tryReceive().isSuccess) {
                            Log.w(TAG, "Unexpected response dropped")
                        }
                        val isSent = webSocket.send(localRecording.data.toByteString())
                        if (!isSent) awaitCancellation() // web socket is closed
                        val responseResult = withTimeoutOrNull(TIMEOUT_RESPONSE) {
                            wsResponseChannel.receiveCatching()
                        }
                        lastResult = if (responseResult == null) {
                            // response timeout is reached
                            RemoteRecognitionResult.Error.BadConnection
                        } else {
                            responseResult.getOrNull()
                                ?.toRecognitionResult(  // valid response
                                    json, localRecording.startTimestamp, localRecording.duration
                                )
                                ?: awaitCancellation() // the channel is closed
                        }
                        unprocessedRecording = null
                    } while (lastResult == RemoteRecognitionResult.NoMatches)
                }.onFailure {  cause ->
                    lastResult = if (isRecoverableWebSocketFailure(cause)) {
                        RemoteRecognitionResult.Error.BadConnection
                    } else {
                        RemoteRecognitionResult.Error.UnhandledError(cause = cause)
                    }
                }
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
        // Cancel current web socket session if it's still alive
        wsResponseChannel.cancel()
        lastResult
    }

    // See okhttp RetryAndFollowUpInterceptor and RealWebSocket
    private fun isRecoverableWebSocketFailure(throwable: Throwable) = when (throwable) {
        is ProtocolException -> false
        is SSLHandshakeException -> throwable.cause !is CertificateException
        is SSLPeerUnverifiedException -> false
        // SocketTimeoutException is used on missing pong frame
        is InterruptedIOException -> throwable is SocketTimeoutException

        is IOException -> true
        else -> false
    }

    @AssistedFactory
    interface Factory {
        fun create(config: AuddConfig): AuddRecognitionService
    }

    companion object {
        private const val REST_BASE_URL = "https://api.audd.io/"
        private const val WEB_SOCKET_TEMPLATE_URL = "wss://api.audd.io/ws/?return=%s&api_token=%s"
        private const val RETURN_PARAM = "lyrics,spotify,apple_music,deezer,napster,musicbrainz"
        private const val RECORDING_MEDIA_TYPE = "audio/mpeg; charset=utf-8"

        private val TIMEOUT_AFTER_RECORDING_FINISHED = 10.seconds
        private val TIMEOUT_RESPONSE = 15.seconds
        private const val WEB_SOCKET_RECONNECT_LIMIT = 1
    }
}
