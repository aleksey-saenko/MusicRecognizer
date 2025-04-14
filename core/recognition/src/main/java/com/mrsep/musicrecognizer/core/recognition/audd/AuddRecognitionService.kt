package com.mrsep.musicrecognizer.core.recognition.audd

import android.util.Log
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.core.domain.preferences.AuddConfig
import com.mrsep.musicrecognizer.core.domain.recognition.RemoteRecognitionService
import com.mrsep.musicrecognizer.core.domain.recognition.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.core.recognition.audd.json.AuddResponseJson
import com.mrsep.musicrecognizer.core.recognition.audd.json.toRecognitionResult
import com.mrsep.musicrecognizer.core.recognition.audd.websocket.AuddWebSocketSession
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
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.WebSocket
import okio.ByteString.Companion.toByteString
import ru.gildor.coroutines.okhttp.await
import java.io.File
import java.io.IOException
import java.io.InterruptedIOException
import java.net.ProtocolException
import java.net.SocketTimeoutException
import java.net.URL
import java.security.cert.CertificateException
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException

private const val TAG = "AuddRecognitionService"

internal class AuddRecognitionService @AssistedInject constructor(
    @Assisted private val config: AuddConfig,
    private val webSocketSession: AuddWebSocketSession,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val okHttpClient: OkHttpClient,
    private val json: Json,
) : RemoteRecognitionService {

    override suspend fun recognize(recording: ByteArray): RemoteRecognitionResult {
        return withContext(ioDispatcher) {
            baseHttpCall { addByteArrayAsMultipartBody(recording) }
        }
    }

    override suspend fun recognize(recording: File): RemoteRecognitionResult {
        return withContext(ioDispatcher) {
            baseHttpCall { addFileAsMultipartBody(recording) }
        }
    }

    suspend fun recognize(url: URL): RemoteRecognitionResult {
        return withContext(ioDispatcher) {
            baseHttpCall { addUrlAsMultipartBody(url) }
        }
    }

    private fun MultipartBody.Builder.addByteArrayAsMultipartBody(
        byteArray: ByteArray,
    ): MultipartBody.Builder {
        return this.addFormDataPart(
            "file",
            "byteArray",
            byteArray.toRequestBody(RECORDING_MEDIA_TYPE.toMediaTypeOrNull())
        )
    }

    private fun MultipartBody.Builder.addFileAsMultipartBody(
        file: File,
    ): MultipartBody.Builder {
        return this.addFormDataPart(
            "file",
            file.name,
            file.asRequestBody(RECORDING_MEDIA_TYPE.toMediaTypeOrNull())
        )
    }

    private fun MultipartBody.Builder.addUrlAsMultipartBody(
        url: URL,
    ): MultipartBody.Builder {
        return this.addFormDataPart("url", url.toExternalForm())
    }

    private suspend inline fun baseHttpCall(
        dataBodyPart: MultipartBody.Builder.() -> MultipartBody.Builder,
    ): RemoteRecognitionResult {
        val multipartBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("api_token", config.apiToken)
            .addFormDataPart("return", RETURN_PARAM)
            .dataBodyPart()
            .build()
        val request = Request.Builder()
            .url(AUDD_REST_BASE_URL)
            .post(multipartBody)
            .build()

        val response = try {
            okHttpClient.newCall(request).await()
        } catch (e: IOException) {
            return RemoteRecognitionResult.Error.BadConnection
        }
        return response.use {
            if (response.isSuccessful) {
                try {
                    json.decodeFromString<AuddResponseJson>(response.body!!.string())
                        .toRecognitionResult(json)
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
    override suspend fun recognize(recordingFlow: Flow<ByteArray>) = withContext(ioDispatcher) {
        val recordingChannel = Channel<ByteArray>(Channel.UNLIMITED)
        var emptyRecordingStream = false
        val recordWithTimeout = launch {
            recordingFlow
                .onEmpty { emptyRecordingStream = true }
                .collect(recordingChannel::send)
            recordingChannel.close()
            delay(if (emptyRecordingStream) 0L else TIMEOUT_AFTER_RECORDING_FINISHED)
        }

        val currentWebSocket = MutableStateFlow<Result<WebSocket>?>(null)
        var failedConnectionCount = 0
        val wsResponseChannel = webSocketSession.startReconnectingSession(config.apiToken)
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

                    is SocketEvent.ResponseReceived -> {
                        emit(socketEvent.response)
                    }
                }
                currentWebSocket.value?.isFailure != true
            }
            .buffer(Channel.UNLIMITED)
            .produceIn(this)

        // Initially bad connection
        // for case when after TIMEOUT_AFTER_RECORDING_FINISHED there is no any response yet
        var lastResult: RemoteRecognitionResult = RemoteRecognitionResult.Error.BadConnection
        var unprocessedRecording: ByteArray? = null
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
                        val isSent = webSocket.send(localRecording.toByteString())
                        if (!isSent) awaitCancellation() // web socket is closed
                        val responseResult = withTimeoutOrNull(TIMEOUT_RESPONSE) {
                            wsResponseChannel.receiveCatching()
                        }
                        lastResult = if (responseResult == null) {
                            // response timeout is reached
                            RemoteRecognitionResult.Error.BadConnection
                        } else {
                            responseResult.getOrNull() // valid response
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
        private const val AUDD_REST_BASE_URL = "https://api.audd.io/"
        private const val RETURN_PARAM = "lyrics,spotify,apple_music,deezer,napster,musicbrainz"
        private const val RECORDING_MEDIA_TYPE = "audio/mpeg; charset=utf-8"

        private const val TIMEOUT_AFTER_RECORDING_FINISHED = 10_000L
        private const val TIMEOUT_RESPONSE = 15_000L
        private const val WEB_SOCKET_RECONNECT_LIMIT = 1
    }
}
