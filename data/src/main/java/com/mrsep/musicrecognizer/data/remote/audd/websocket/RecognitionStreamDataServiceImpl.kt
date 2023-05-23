package com.mrsep.musicrecognizer.data.remote.audd.websocket

import android.util.Log
import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionDataResult
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
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.WebSocket
import okio.ByteString.Companion.toByteString
import javax.inject.Inject

private const val TAG = "RemoteRecognitionStreamServiceImpl"

private const val TIMEOUT_AFTER_RECORDING_FINISHED = 5_000L
private const val TIMEOUT_RECORDING_SENDING = 5_000L
private const val WEB_SOCKET_RECONNECT_LIMIT = 1

private enum class SendingJobResult {
    BadRecording, SendingTimeoutExpired, Success
}

@OptIn(ExperimentalCoroutinesApi::class)
class RecognitionStreamDataServiceImpl @Inject constructor(
    private val webSocketService: AuddRecognitionWebSocketService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : RecognitionStreamDataService {


    override suspend fun recognize(
        token: String,
        requiredServices: UserPreferencesProto.RequiredServicesProto,
        audioRecordingFlow: Flow<ByteArray>
    ): RemoteRecognitionDataResult {
        return withContext(ioDispatcher) {

            val recordingChannel = Channel<ByteArray>(Channel.UNLIMITED)

            val recordWithTimeoutJob = launch {
                audioRecordingFlow.collect { recordingChannel.send(it) }
                recordingChannel.close()
                Log.d(TAG, "Final timeout started")
                delay(TIMEOUT_AFTER_RECORDING_FINISHED)
                Log.d(TAG, "Final timeout finished")
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
                                    Log.d(
                                        TAG,
                                        "Recording #$sentCounter was added to sending queue=$isSent"
                                    )
                                    if (!isSent) awaitCancellation()
                                    while (wsService.queueSize() > 0) {
                                        Log.d(TAG, "queueSize > 0")
                                        delay(100)
                                    }
                                    Log.d(TAG, "Recording #$sentCounter was sent")
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
                        RemoteRecognitionDataResult.Error.BadRecording(
                            "Recognition process failed due to empty audio recording stream"
                        )

                    SendingJobResult.SendingTimeoutExpired ->
                        RemoteRecognitionDataResult.Error.BadConnection
                }
            }


            var retryCounter = 0
            var responseCounter = 0
            var lastResponse: RemoteRecognitionDataResult =
                RemoteRecognitionDataResult.Error.BadConnection
            // return only with final response, else continue handling sockets events
            val finalResponseAsync = async {
                webSocketService.startSession(token, requiredServices)
                    .onEach { Log.d(TAG, "Get socket event=${it::class.simpleName}") }
                    .transform { socketEvent ->
                        when (socketEvent) {
                            is SocketEvent.ConnectionClosing,
                            is SocketEvent.ConnectionClosed,
                            is SocketEvent.ConnectionFailed -> {
                                lastResponse = RemoteRecognitionDataResult.Error.BadConnection
                                if (retryCounter == WEB_SOCKET_RECONNECT_LIMIT) {
                                    emit(RemoteRecognitionDataResult.Error.BadConnection)
                                }
                                retryCounter++
                            }

                            is SocketEvent.ConnectionOpened -> {
                                webSocketStateFlow.update { socketEvent.webSocket }
                            }

                            is SocketEvent.ResponseReceived -> {
                                Log.d(TAG, "Response #$responseCounter received: ${socketEvent.response::class.simpleName}")
                                responseCounter++
                                when (socketEvent.response) {
                                    is RemoteRecognitionDataResult.Success,
                                    is RemoteRecognitionDataResult.Error.WrongToken,
                                    is RemoteRecognitionDataResult.Error.BadRecording -> {
                                        emit(socketEvent.response)
                                    }

                                    else -> {
                                        Log.d(TAG, "sendingJob.isCompleted=${sendingJob.isCompleted}, responseCounter=$responseCounter, sentCounter=$sentCounter")
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
                    Log.d(TAG, "Selected clause: Final response")
                    sendingJob.cancelAndJoin()
                    badSendingResultAsync.cancelAndJoin()
                    recordWithTimeoutJob.cancelAndJoin()
                    finalResult
                }

                badSendingResultAsync.onAwait { finalResult ->
                    Log.d(TAG, "Selected clause: Bad sending")
                    finalResponseAsync.cancelAndJoin()
                    sendingJob.cancelAndJoin()
                    recordWithTimeoutJob.cancelAndJoin()
                    finalResult
                }

                recordWithTimeoutJob.onJoin {
                    Log.d(TAG, "Selected clause: Final timeout")
                    finalResponseAsync.cancelAndJoin()
                    sendingJob.cancelAndJoin()
                    badSendingResultAsync.cancelAndJoin()
                    lastResponse
                }

            }

        }
    }


}