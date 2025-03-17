package com.mrsep.musicrecognizer.core.recognition

import com.mrsep.musicrecognizer.core.domain.preferences.AuddConfig
import com.mrsep.musicrecognizer.core.domain.recognition.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.core.recognition.audd.AuddRecognitionService
import com.mrsep.musicrecognizer.core.recognition.audd.websocket.AuddWebSocketSession
import com.mrsep.musicrecognizer.core.recognition.audd.websocket.SocketEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okio.ByteString
import org.junit.Assert
import org.junit.Test

private const val TIMEOUT_AFTER_RECORDING_FINISHED = 10_000L
private const val TIMEOUT_RECORDING_SENDING = 8_000L

private const val TEST_QUEUE_DELAY = 450L
private const val TEST_RESPONSE_DELAY = 600L
private const val TEST_SOCKET_OPENING_DELAY = 2300L

// take into account this delay in test subject
// while (wsService.queueSize() > 0) {
//     delay(100)
// }

@OptIn(ExperimentalCoroutinesApi::class)
class AuddRecognitionServiceTest {

    private val scope = TestScope()
    private val testDispatcher = StandardTestDispatcher(scope.testScheduler)

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }
    private val config = AuddConfig(apiToken = "")
    private val okHttpClient = OkHttpClient()

    @Test
    fun `intermediate results only = return last`() = scope.runTest {
        val webSocket = object : WebSocketBaseFake(this) {
            var firstResponse = true
            override fun send(bytes: ByteString): Boolean {
                queueSize += bytes.size.toLong()
                launch {
                    clearQueue(bytes, TEST_QUEUE_DELAY)
                    val result = if (firstResponse) {
                        RemoteRecognitionResult.NoMatches
                    } else {
                        RemoteRecognitionResult.Error.UnhandledError()
                    }
                    firstResponse = false
                    delay(TEST_RESPONSE_DELAY)
                    outputChannel.send(SocketEvent.ResponseReceived(result))
                }
                return true
            }
        }
        webSocket.openSocketWithDelay(0)
        val service = AuddRecognitionService(
            config = config,
            webSocketSession = wrapSocketToService(webSocket),
            ioDispatcher = testDispatcher,
            okHttpClient = okHttpClient,
            json = json,
        )
        val result = service.recognize(normalAudioRecordingFlow)
        val expectedTime = normalAudioFlowDuration + TEST_QUEUE_DELAY + TEST_RESPONSE_DELAY
        Assert.assertTrue(result is RemoteRecognitionResult.Error.UnhandledError)
        Assert.assertEquals(expectedTime, testScheduler.currentTime)
    }

    @Test
    fun `success received = return immediately`() = scope.runTest {
        val webSocket = object : WebSocketBaseFake(this) {
            override fun send(bytes: ByteString): Boolean {
                queueSize += bytes.size.toLong()
                launch {
                    clearQueue(bytes, TEST_QUEUE_DELAY)
                    val result = RemoteRecognitionResult.Success(fakeTrack)
                    delay(TEST_RESPONSE_DELAY)
                    outputChannel.send(SocketEvent.ResponseReceived(result))
                }
                return true
            }
        }
        webSocket.openSocketWithDelay(TEST_SOCKET_OPENING_DELAY)
        val service = AuddRecognitionService(
            config = config,
            webSocketSession = wrapSocketToService(webSocket),
            ioDispatcher = testDispatcher,
            okHttpClient = okHttpClient,
            json = json,
        )
        val result = service.recognize(normalAudioRecordingFlow)
        val expectedTime = TEST_SOCKET_OPENING_DELAY + TEST_QUEUE_DELAY + TEST_RESPONSE_DELAY
        Assert.assertTrue(result is RemoteRecognitionResult.Success)
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
                    outputChannel.send(
                        SocketEvent.ResponseReceived(
                            RemoteRecognitionResult.Error.ApiUsageLimited
                        )
                    )
                }
                return true
            }
        }
        webSocket.openSocketWithDelay(TEST_SOCKET_OPENING_DELAY)
        val service = AuddRecognitionService(
            config = config,
            webSocketSession = wrapSocketToService(webSocket),
            ioDispatcher = testDispatcher,
            okHttpClient = okHttpClient,
            json = json,
        )
        val result = service.recognize(normalAudioRecordingFlow)
        val expected = RemoteRecognitionResult.Error.ApiUsageLimited
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
                    outputChannel.send(
                        SocketEvent.ResponseReceived(RemoteRecognitionResult.NoMatches)
                    )
                }
                return true
            }
        }
        webSocket.openSocketWithDelay(0)
        val service = AuddRecognitionService(
            config = config,
            webSocketSession = wrapSocketToService(webSocket),
            ioDispatcher = testDispatcher,
            okHttpClient = okHttpClient,
            json = json,
        )
        val result = service.recognize(normalAudioRecordingFlow)
        val expectedTime = normalAudioFlowDuration + TIMEOUT_AFTER_RECORDING_FINISHED
        Assert.assertTrue(result is RemoteRecognitionResult.NoMatches)
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
                    outputChannel.send(
                        SocketEvent.ResponseReceived(
                            RemoteRecognitionResult.NoMatches
                        )
                    )
                }
                return true
            }
        }

        webSocket.openSocketWithDelay(0)
        webSocket.simulateReconnect(1000, 500)
        val delayBeforeSecondFailure = 2000L
        webSocket.simulateReconnect(delayBeforeSecondFailure, 500)
        val service = AuddRecognitionService(
            config = config,
            webSocketSession = wrapSocketToService(webSocket),
            ioDispatcher = testDispatcher,
            okHttpClient = okHttpClient,
            json = json,
        )
        val result = service.recognize(normalAudioRecordingFlow)
        Assert.assertTrue(result is RemoteRecognitionResult.Error.BadConnection)
        Assert.assertEquals(delayBeforeSecondFailure, testScheduler.currentTime)
    }

    @Test
    fun `sending limit is expired = return bad connection`() = scope.runTest {
        val webSocket = object : WebSocketBaseFake(this) {
            override fun send(bytes: ByteString): Boolean {
                queueSize += bytes.size.toLong()
                launch {
                    clearQueue(bytes, TIMEOUT_RECORDING_SENDING + 500)
                    delay(TEST_RESPONSE_DELAY)
                    outputChannel.send(
                        SocketEvent.ResponseReceived(
                            RemoteRecognitionResult.Error.ApiUsageLimited
                        )
                    )
                }
                return true
            }
        }
        webSocket.openSocketWithDelay(TEST_SOCKET_OPENING_DELAY)
        val service = AuddRecognitionService(
            config = config,
            webSocketSession = wrapSocketToService(webSocket),
            ioDispatcher = testDispatcher,
            okHttpClient = okHttpClient,
            json = json,
        )
        val result = service.recognize(normalAudioRecordingFlow)
        val expectedTime = TIMEOUT_RECORDING_SENDING
        Assert.assertTrue(result is RemoteRecognitionResult.Error.BadConnection)
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
                    outputChannel.send(
                        SocketEvent.ResponseReceived(
                            RemoteRecognitionResult.Error.ApiUsageLimited
                        )
                    )
                }
                return true
            }
        }
        webSocket.openSocketWithDelay(TEST_SOCKET_OPENING_DELAY)
        val service = AuddRecognitionService(
            config = config,
            webSocketSession = wrapSocketToService(webSocket),
            ioDispatcher = testDispatcher,
            okHttpClient = okHttpClient,
            json = json,
        )
        val delayBeforeFlowClose = 5500L
        val result = service.recognize(emptyAudioRecordingFlow(delayBeforeFlowClose))
        Assert.assertTrue(result is RemoteRecognitionResult.Error.BadRecording)
        Assert.assertEquals(delayBeforeFlowClose, testScheduler.currentTime)
    }
}

private fun wrapSocketToService(webSocket: WebSocketBaseFake) = object : AuddWebSocketSession {

    override suspend fun startSession(apiToken: String): Flow<SocketEvent> = flow {
        emitAll(webSocket.outputChannel)
    }
}
