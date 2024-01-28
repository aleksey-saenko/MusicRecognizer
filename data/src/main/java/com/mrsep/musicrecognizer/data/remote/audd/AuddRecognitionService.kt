package com.mrsep.musicrecognizer.data.remote.audd

import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.data.remote.AuddConfigDo
import com.mrsep.musicrecognizer.data.remote.RecognitionServiceDo
import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionResultDo
import com.mrsep.musicrecognizer.data.remote.audd.json.AuddResponseJson
import com.mrsep.musicrecognizer.data.remote.audd.json.toRecognitionResult
import com.mrsep.musicrecognizer.data.remote.audd.websocket.AuddWebSocketSession
import com.mrsep.musicrecognizer.data.remote.audd.websocket.SocketEvent
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
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
import java.net.URL

internal class AuddRecognitionService @AssistedInject constructor(
    @Assisted private val config: AuddConfigDo,
    private val webSocketSession: AuddWebSocketSession,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val okHttpClient: OkHttpClient,
    moshi: Moshi
) : RecognitionServiceDo {

    @OptIn(ExperimentalStdlibApi::class)
    private val responseJsonAdapter = moshi.adapter<AuddResponseJson>()

    override suspend fun recognize(recording: ByteArray): RemoteRecognitionResultDo {
        return withContext(ioDispatcher) {
            baseHttpCall { addByteArrayAsMultipartBody(recording) }
        }
    }

    override suspend fun recognize(recording: File): RemoteRecognitionResultDo {
        return withContext(ioDispatcher) {
            baseHttpCall { addFileAsMultipartBody(recording) }
        }
    }

    override suspend fun recognize(url: URL): RemoteRecognitionResultDo {
        return withContext(ioDispatcher) {
            baseHttpCall { addUrlAsMultipartBody(url) }
        }
    }

    private fun MultipartBody.Builder.addByteArrayAsMultipartBody(
        byteArray: ByteArray
    ): MultipartBody.Builder {
        return this.addFormDataPart(
            "file",
            "byteArray",
            byteArray.toRequestBody(RECORDING_MEDIA_TYPE.toMediaTypeOrNull())
        )
    }

    private fun MultipartBody.Builder.addFileAsMultipartBody(
        file: File
    ): MultipartBody.Builder {
        return this.addFormDataPart(
            "file",
            file.name,
            file.asRequestBody(RECORDING_MEDIA_TYPE.toMediaTypeOrNull())
        )
    }

    private fun MultipartBody.Builder.addUrlAsMultipartBody(
        url: URL
    ): MultipartBody.Builder {
        return this.addFormDataPart("url", url.toExternalForm())
    }

    private suspend inline fun baseHttpCall(
        dataBodyPart: MultipartBody.Builder.() -> MultipartBody.Builder
    ): RemoteRecognitionResultDo {
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
        return try {
            val response = okHttpClient.newCall(request).await()
            if (response.isSuccessful) {
                try {
                    val responseJson = responseJsonAdapter.fromJson(response.body!!.source())!!
                    responseJson.toRecognitionResult()
                } catch (e: Exception) {
                    e.printStackTrace()
                    RemoteRecognitionResultDo.Error.UnhandledError(message = e.message ?: "", e = e)
                }
            } else {
                RemoteRecognitionResultDo.Error.HttpError(
                    code = response.code,
                    message = response.message
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
            RemoteRecognitionResultDo.Error.BadConnection
        } catch (e: Exception) {
            e.printStackTrace()
            RemoteRecognitionResultDo.Error.UnhandledError(message = e.message ?: "", e = e)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun recognize(
        recordingFlow: Flow<ByteArray>
    ): RemoteRecognitionResultDo {
        return withContext(ioDispatcher) {
            val recordingChannel = Channel<ByteArray>(Channel.UNLIMITED)

            val recordWithTimeoutJob = launch {
                recordingFlow.collect { recordingChannel.send(it) }
                recordingChannel.close()
                delay(TIMEOUT_AFTER_RECORDING_FINISHED)
            }

            val webSocketStateFlow = MutableStateFlow<WebSocket?>(null)

            var sentCounter = 0
            val sendingJob = async {
                var recordingReceived = false
                recordingChannel.receiveAsFlow().transform { recording ->
                    recordingReceived = true
                    try {
                        withTimeout(TIMEOUT_RECORDING_SENDING) {
                            webSocketStateFlow.filterNotNull()
                                .transformLatest { wsService ->
                                    val isSent = wsService.send(recording.toByteString())
                                    if (!isSent) awaitCancellation()
                                    while (wsService.queueSize() > 0) {
                                        delay(100)
                                    }
                                    this.emit(Unit)
                                }.first()
                            sentCounter++
                        }
                    } catch (e: TimeoutCancellationException) {
                        this.emit(SendingJobResult.SendingTimeoutExpired)
                    }
                }.firstOrNull()
                    ?: if (recordingReceived) {
                        SendingJobResult.Success
                    } else {
                        SendingJobResult.BadRecording
                    }
            }

            // return only with bad result occurred during recording sending, else suspend
            val badSendingResultAsync = async {
                when (sendingJob.await()) {
                    SendingJobResult.Success -> awaitCancellation()
                    SendingJobResult.BadRecording ->
                        RemoteRecognitionResultDo.Error.BadRecording(
                            "Recognition process failed due to empty audio recording stream"
                        )

                    SendingJobResult.SendingTimeoutExpired ->
                        RemoteRecognitionResultDo.Error.BadConnection
                }
            }

            var retryCounter = 0
            var responseCounter = 0
            var lastResponse: RemoteRecognitionResultDo =
                RemoteRecognitionResultDo.Error.BadConnection
            // return only with final response, else continue handling sockets events
            val finalResponseAsync = async {
                webSocketSession.startSession(config.apiToken)
                    .transform { socketEvent ->
                        when (socketEvent) {
                            is SocketEvent.ConnectionClosing,
                            is SocketEvent.ConnectionClosed -> { /* NO-OP */ }
                            is SocketEvent.ConnectionFailed -> {
                                lastResponse = RemoteRecognitionResultDo.Error.BadConnection
                                if (retryCounter == WEB_SOCKET_RECONNECT_LIMIT) {
                                    emit(RemoteRecognitionResultDo.Error.BadConnection)
                                }
                                retryCounter++
                            }

                            is SocketEvent.ConnectionOpened -> {
                                webSocketStateFlow.update { socketEvent.webSocket }
                            }

                            is SocketEvent.ResponseReceived -> {
                                responseCounter++
                                when (socketEvent.response) {
                                    is RemoteRecognitionResultDo.Success,
                                    is RemoteRecognitionResultDo.Error.AuthError,
                                    is RemoteRecognitionResultDo.Error.ApiUsageLimited,
                                    is RemoteRecognitionResultDo.Error.BadRecording -> {
                                        emit(socketEvent.response)
                                    }

                                    else -> {
                                        if (sendingJob.isCompleted && responseCounter == sentCounter) {
                                            emit(socketEvent.response)
                                        } else {
                                            lastResponse = socketEvent.response
                                        }
                                    }
                                }
                            }
                        }
                    }
                    .first()
            }

            select {
                finalResponseAsync.onAwait { finalResult ->
                    sendingJob.cancelAndJoin()
                    badSendingResultAsync.cancelAndJoin()
                    recordWithTimeoutJob.cancelAndJoin()
                    finalResult
                }
                badSendingResultAsync.onAwait { finalResult ->
                    finalResponseAsync.cancelAndJoin()
                    sendingJob.cancelAndJoin()
                    recordWithTimeoutJob.cancelAndJoin()
                    finalResult
                }
                recordWithTimeoutJob.onJoin {
                    finalResponseAsync.cancelAndJoin()
                    sendingJob.cancelAndJoin()
                    badSendingResultAsync.cancelAndJoin()
                    lastResponse
                }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(config: AuddConfigDo): AuddRecognitionService
    }

    companion object {
        private const val AUDD_REST_BASE_URL = "https://api.audd.io/"
        private const val RETURN_PARAM = "lyrics,spotify,apple_music,deezer,napster,musicbrainz"
        private const val RECORDING_MEDIA_TYPE = "audio/mpeg; charset=utf-8"

        private const val TIMEOUT_AFTER_RECORDING_FINISHED = 10_000L
        private const val TIMEOUT_RECORDING_SENDING = 8_000L
        private const val WEB_SOCKET_RECONNECT_LIMIT = 1
    }

}

private enum class SendingJobResult {
    BadRecording, SendingTimeoutExpired, Success
}