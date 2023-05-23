package com.mrsep.musicrecognizer.data

import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionDataResult
import com.mrsep.musicrecognizer.data.remote.audd.websocket.AuddRecognitionWebSocketService
import com.mrsep.musicrecognizer.data.remote.audd.websocket.RecognitionStreamDataServiceImpl
import com.mrsep.musicrecognizer.data.remote.audd.websocket.SocketEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okio.ByteString
import org.junit.Assert
import org.junit.Test

// test socket replacement

private const val TIMEOUT_AFTER_RECORDING_FINISHED = 5_000L
private const val TIMEOUT_RECORDING_SENDING = 5_000L

private const val TEST_QUEUE_DELAY = 450L
private const val TEST_RESPONSE_DELAY = 600L
private const val TEST_SOCKET_OPENING_DELAY = 2300L

// take into account this delay in test subject
// while (wsService.queueSize() > 0) {
//     Log.d(TAG, "queueSize > 0")
//     delay(100)
// }

@OptIn(ExperimentalCoroutinesApi::class)
class RecognitionStreamDataServiceImplTest {

    private val scope = TestScope()
    private val testDispatcher = StandardTestDispatcher(scope.testScheduler)

    @Test
    fun `intermediate results only = return last`() = scope.runTest {
        val webSocket = object : WebSocketBaseFake(this) {
            var firstResponse = true
            override fun send(bytes: ByteString): Boolean {
                queueSize += bytes.size.toLong()
                launch {
                    clearQueue(bytes, TEST_QUEUE_DELAY)
                    val result = if (firstResponse) {
                        RemoteRecognitionDataResult.NoMatches
                    } else {
                        RemoteRecognitionDataResult.Error.UnhandledError()
                    }
                    firstResponse = false
                    delay(TEST_RESPONSE_DELAY)
                    outputChannel.send(SocketEvent.ResponseReceived(result))
                }
                return true
            }
        }
        webSocket.openSocketWithDelay(0)
        val service = RecognitionStreamDataServiceImpl(
            webSocketService = wrapSocketToService(webSocket),
            ioDispatcher = testDispatcher
        )
        val result = service.recognize(
            token = "",
            requiredServices = UserPreferencesProto.RequiredServicesProto.getDefaultInstance(),
            audioRecordingFlow = normalAudioRecordingFlow
        )
        val expectedTime = normalAudioFlowDuration + TEST_QUEUE_DELAY + TEST_RESPONSE_DELAY
        Assert.assertTrue(result is RemoteRecognitionDataResult.Error.UnhandledError)
        Assert.assertEquals(expectedTime, testScheduler.currentTime)
    }

    @Test
    fun `success received = return immediately`() = scope.runTest {
        val webSocket = object : WebSocketBaseFake(this) {
            override fun send(bytes: ByteString): Boolean {
                queueSize += bytes.size.toLong()
                launch {
                    clearQueue(bytes, TEST_QUEUE_DELAY)
                    val result = RemoteRecognitionDataResult.Success(fakeTrack)
                    delay(TEST_RESPONSE_DELAY)
                    outputChannel.send(SocketEvent.ResponseReceived(result))
                }
                return true
            }
        }
        webSocket.openSocketWithDelay(TEST_SOCKET_OPENING_DELAY)
        val service = RecognitionStreamDataServiceImpl(
            webSocketService = wrapSocketToService(webSocket),
            ioDispatcher = testDispatcher
        )
        val result = service.recognize(
            token = "",
            requiredServices = UserPreferencesProto.RequiredServicesProto.getDefaultInstance(),
            audioRecordingFlow = normalAudioRecordingFlow
        )
        val expectedTime = TEST_SOCKET_OPENING_DELAY + TEST_QUEUE_DELAY + TEST_RESPONSE_DELAY
        Assert.assertTrue(result is RemoteRecognitionDataResult.Success)
        Assert.assertEquals(expectedTime, testScheduler.currentTime)
    }

    @Test
    fun `wrong token = return immediately`() = scope.runTest {
        val webSocket = object : WebSocketBaseFake(this) {
            override fun send(bytes: ByteString): Boolean {
                queueSize += bytes.size.toLong()
                launch {
                    clearQueue(bytes, TEST_QUEUE_DELAY)
                    delay(TEST_RESPONSE_DELAY)
                    outputChannel.send(SocketEvent.ResponseReceived(
                        RemoteRecognitionDataResult.Error.WrongToken(true))
                    )
                }
                return true
            }
        }
        webSocket.openSocketWithDelay(TEST_SOCKET_OPENING_DELAY)
        val service = RecognitionStreamDataServiceImpl(
            webSocketService = wrapSocketToService(webSocket),
            ioDispatcher = testDispatcher
        )
        val result = service.recognize(
            token = "",
            requiredServices = UserPreferencesProto.RequiredServicesProto.getDefaultInstance(),
            audioRecordingFlow = normalAudioRecordingFlow
        )
        val expected = RemoteRecognitionDataResult.Error.WrongToken(true)
        val expectedTime = TEST_SOCKET_OPENING_DELAY + TEST_QUEUE_DELAY + TEST_RESPONSE_DELAY
        Assert.assertEquals(expected, result)
        Assert.assertEquals(expectedTime, testScheduler.currentTime)
    }

    @Test
    fun `total timeout is expired = return last`() = scope.runTest {
        val webSocket = object : WebSocketBaseFake(this) {
            override fun send(bytes: ByteString): Boolean {
                queueSize += bytes.size.toLong()
                launch {
                    clearQueue(bytes, 4500)
                    delay(TEST_RESPONSE_DELAY)
                    outputChannel.send(SocketEvent.ResponseReceived(
                        RemoteRecognitionDataResult.NoMatches)
                    )
                }
                return true
            }
        }
        webSocket.openSocketWithDelay(0)
        val service = RecognitionStreamDataServiceImpl(
            webSocketService = wrapSocketToService(webSocket),
            ioDispatcher = testDispatcher
        )
        val result = service.recognize(
            token = "",
            requiredServices = UserPreferencesProto.RequiredServicesProto.getDefaultInstance(),
            audioRecordingFlow = normalAudioRecordingFlow
        )
        val expectedTime = normalAudioFlowDuration + TIMEOUT_AFTER_RECORDING_FINISHED
        Assert.assertTrue(result is RemoteRecognitionDataResult.NoMatches)
        Assert.assertEquals(expectedTime, testScheduler.currentTime)
    }

    @Test
    fun `reconnect limit is expired = return bad connection`() = scope.runTest {
        val webSocket = object : WebSocketBaseFake(this) {
            override fun send(bytes: ByteString): Boolean {
                queueSize += bytes.size.toLong()
                launch {
                    clearQueue(bytes, 0)
                    delay(TEST_RESPONSE_DELAY)
                    outputChannel.send(SocketEvent.ResponseReceived(
                        RemoteRecognitionDataResult.NoMatches)
                    )
                }
                return true
            }
        }

        webSocket.openSocketWithDelay(0)
        webSocket.simulateReconnect(1000, 500)
        val delayBeforeSecondFailure = 2000L
        webSocket.simulateReconnect(delayBeforeSecondFailure, 500)
        val service = RecognitionStreamDataServiceImpl(
            webSocketService = wrapSocketToService(webSocket),
            ioDispatcher = testDispatcher
        )
        val result = service.recognize(
            token = "",
            requiredServices = UserPreferencesProto.RequiredServicesProto.getDefaultInstance(),
            audioRecordingFlow = normalAudioRecordingFlow
        )
        Assert.assertTrue(result is RemoteRecognitionDataResult.Error.BadConnection)
        Assert.assertEquals(delayBeforeSecondFailure, testScheduler.currentTime)
    }

    @Test
    fun `sending limit is expired = return bad connection`() = scope.runTest {
        val webSocket = object : WebSocketBaseFake(this) {
            override fun send(bytes: ByteString): Boolean {
                queueSize += bytes.size.toLong()
                launch {
                    clearQueue(bytes, 6_000)
                    delay(TEST_RESPONSE_DELAY)
                    outputChannel.send(SocketEvent.ResponseReceived(
                        RemoteRecognitionDataResult.Error.WrongToken(true))
                    )
                }
                return true
            }
        }
        webSocket.openSocketWithDelay(TEST_SOCKET_OPENING_DELAY)
        val service = RecognitionStreamDataServiceImpl(
            webSocketService = wrapSocketToService(webSocket),
            ioDispatcher = testDispatcher
        )
        val result = service.recognize(
            token = "",
            requiredServices = UserPreferencesProto.RequiredServicesProto.getDefaultInstance(),
            audioRecordingFlow = normalAudioRecordingFlow
        )
        val expectedTime = TIMEOUT_RECORDING_SENDING
        Assert.assertTrue(result is RemoteRecognitionDataResult.Error.BadConnection)
        Assert.assertEquals(expectedTime, testScheduler.currentTime)
    }



    @Test
    fun `empty audio recording flow = return bad recording`() = scope.runTest {
        val webSocket = object : WebSocketBaseFake(this) {
            override fun send(bytes: ByteString): Boolean {
                queueSize += bytes.size.toLong()
                launch {
                    clearQueue(bytes, TEST_QUEUE_DELAY)
                    delay(TEST_RESPONSE_DELAY)
                    outputChannel.send(SocketEvent.ResponseReceived(
                        RemoteRecognitionDataResult.Error.WrongToken(true))
                    )
                }
                return true
            }
        }
        webSocket.openSocketWithDelay(TEST_SOCKET_OPENING_DELAY)
        val service = RecognitionStreamDataServiceImpl(
            webSocketService = wrapSocketToService(webSocket),
            ioDispatcher = testDispatcher
        )
        val delayBeforeFlowClose = 5500L
        val result = service.recognize(
            token = "",
            requiredServices = UserPreferencesProto.RequiredServicesProto.getDefaultInstance(),
            audioRecordingFlow = emptyAudioRecordingFlow(delayBeforeFlowClose)
        )
        Assert.assertTrue(result is RemoteRecognitionDataResult.Error.BadRecording)
        Assert.assertEquals(delayBeforeFlowClose, testScheduler.currentTime)
    }


}

// must provide websocket events while collected (reconnect on close by manual simulate)
private fun wrapSocketToService(webSocket: WebSocketBaseFake) = object : AuddRecognitionWebSocketService {

    override suspend fun startSession(
        token: String,
        requiredServices: UserPreferencesProto.RequiredServicesProto
    ): Flow<SocketEvent> = flow {
        emitAll(webSocket.outputChannel)
    }

}