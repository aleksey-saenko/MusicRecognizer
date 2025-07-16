package com.mrsep.musicrecognizer.core.recognition

import com.mrsep.musicrecognizer.core.domain.preferences.AuddConfig
import com.mrsep.musicrecognizer.core.domain.recognition.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.core.recognition.audd.AuddRecognitionService
import com.mrsep.musicrecognizer.core.recognition.audd.websocket.WebSocketSession
import com.mrsep.musicrecognizer.core.recognition.audd.websocket.SocketEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
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

private const val TEST_RESPONSE_DELAY = 600L
private const val TEST_SOCKET_OPENING_DELAY = 2300L

@OptIn(ExperimentalCoroutinesApi::class)
class AuddRecognitionServiceTest {

    private val scope = TestScope()
    private val testDispatcher = StandardTestDispatcher(scope.testScheduler)

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }
    private val config = AuddConfig(apiToken = "")
    private val okHttpClient = object : dagger.Lazy<OkHttpClient> {
        private val instance = OkHttpClient()
        override fun get(): OkHttpClient = instance
    }
    @Test
    fun `first success = Success immediately`() = scope.runTest {
        val webSocket = object : WebSocketBaseFake(this) {
            override fun send(bytes: ByteString): Boolean {
                launch {
                    delay(TEST_RESPONSE_DELAY)
                    outputChannel.send(
                        SocketEvent.MessageReceived(getJson("audd_response_success.json"))
                    )
                }
                return true
            }
        }
        webSocket.openConnection(TEST_SOCKET_OPENING_DELAY)
        val service = AuddRecognitionService(
            config = config,
            webSocketSession = wrapSocketToService(webSocket),
            ioDispatcher = testDispatcher,
            okHttpClient = okHttpClient,
            json = json,
        )
        val result = service.recognizeFirst(normalAudioRecordingFlow)
        val expectedTime = TEST_SOCKET_OPENING_DELAY + TEST_RESPONSE_DELAY
        Assert.assertTrue(result is RemoteRecognitionResult.Success)
        Assert.assertEquals(expectedTime, testScheduler.currentTime)
    }

    @Test
    fun `no matches results only = return last NoMatches`() = scope.runTest {
        val webSocket = object : WebSocketBaseFake(this) {
            var firstResponse = true
            override fun send(bytes: ByteString): Boolean {
                launch {
                    firstResponse = false
                    delay(TEST_RESPONSE_DELAY)
                    outputChannel.send(
                        SocketEvent.MessageReceived(getJson("audd_response_no_matches.json"))
                    )
                }
                return true
            }
        }
        webSocket.openConnection(0)
        val service = AuddRecognitionService(
            config = config,
            webSocketSession = wrapSocketToService(webSocket),
            ioDispatcher = testDispatcher,
            okHttpClient = okHttpClient,
            json = json,
        )
        val result = service.recognizeFirst(normalAudioRecordingFlow)
        val expectedTime = normalAudioFlowDuration + TEST_RESPONSE_DELAY
        Assert.assertTrue(result is RemoteRecognitionResult.NoMatches)
        Assert.assertEquals(expectedTime, testScheduler.currentTime)
    }

    @Test
    fun `unrecoverable connection failure = UnhandledError immediately`() = scope.runTest {
        val webSocket = object : WebSocketBaseFake(this) {
            override fun send(bytes: ByteString) = false
        }
        val failureTime = 500L
        webSocket.failureConnection(failureTime, false)
        val service = AuddRecognitionService(
            config = config,
            webSocketSession = wrapSocketToService(webSocket),
            ioDispatcher = testDispatcher,
            okHttpClient = okHttpClient,
            json = json,
        )
        val result = service.recognizeFirst(normalAudioRecordingFlow)
        Assert.assertTrue(result is RemoteRecognitionResult.Error.UnhandledError)
        Assert.assertEquals(failureTime, testScheduler.currentTime)
    }

    @Test
    fun `recoverable connection failure = BadConnection immediately`() = scope.runTest {
        val webSocket = object : WebSocketBaseFake(this) {
            override fun send(bytes: ByteString) = false
        }
        val secondFailureTime = 800L
        webSocket.simulateReconnect(secondFailureTime - 300L, 0)
        webSocket.failureConnection(secondFailureTime, true)
        val service = AuddRecognitionService(
            config = config,
            webSocketSession = wrapSocketToService(webSocket),
            ioDispatcher = testDispatcher,
            okHttpClient = okHttpClient,
            json = json,
        )
        val result = service.recognizeFirst(normalAudioRecordingFlow)
        Assert.assertTrue(result is RemoteRecognitionResult.Error.BadConnection)
        Assert.assertEquals(secondFailureTime, testScheduler.currentTime)
    }

    @Test
    fun `empty recording flow = BadRecording immediately`() = scope.runTest {
        val webSocket = object : WebSocketBaseFake(this) {
            override fun send(bytes: ByteString) = false
        }
        webSocket.openConnection(1000L)
        val service = AuddRecognitionService(
            config = config,
            webSocketSession = wrapSocketToService(webSocket),
            ioDispatcher = testDispatcher,
            okHttpClient = okHttpClient,
            json = json,
        )
        val result = service.recognizeFirst(emptyFlow())
        Assert.assertTrue(result is RemoteRecognitionResult.Error.BadRecording)
        Assert.assertEquals(0, testScheduler.currentTime)
    }

    @Test
    fun `wrong token = AuthError`() = scope.runTest {
        val webSocket = object : WebSocketBaseFake(this) {
            override fun send(bytes: ByteString): Boolean {
                launch {
                    delay(TEST_RESPONSE_DELAY)
                    outputChannel.send(
                        SocketEvent.MessageReceived(getJson("audd_response_auth_error.json"))
                    )
                }
                return true
            }
        }
        webSocket.openConnection(TEST_SOCKET_OPENING_DELAY)
        val service = AuddRecognitionService(
            config = config,
            webSocketSession = wrapSocketToService(webSocket),
            ioDispatcher = testDispatcher,
            okHttpClient = okHttpClient,
            json = json,
        )
        val result = service.recognizeFirst(normalAudioRecordingFlow)
        val expected = RemoteRecognitionResult.Error.AuthError
        val expectedTime = TEST_SOCKET_OPENING_DELAY + TEST_RESPONSE_DELAY
        Assert.assertEquals(expected, result)
        Assert.assertEquals(expectedTime, testScheduler.currentTime)
    }

    @Test
    fun `total timeout is reached = last result`() = scope.runTest {
        val webSocket = object : WebSocketBaseFake(this) {
            var firstMessage = true
            override fun send(bytes: ByteString): Boolean {
                launch {
                    if (firstMessage) {
                        delay(5_000)
                    } else {
                        delay(10_000)
                    }
                    outputChannel.send(
                        SocketEvent.MessageReceived(getJson("audd_response_no_matches.json"))
                    )
                }
                return true
            }
        }
        webSocket.openConnection(0)
        val service = AuddRecognitionService(
            config = config,
            webSocketSession = wrapSocketToService(webSocket),
            ioDispatcher = testDispatcher,
            okHttpClient = okHttpClient,
            json = json,
        )
        val result = service.recognizeFirst(normalAudioRecordingFlow)
        val expectedTime = normalAudioFlowDuration + TIMEOUT_AFTER_RECORDING_FINISHED
        Assert.assertTrue(result is RemoteRecognitionResult.NoMatches)
        Assert.assertEquals(expectedTime, testScheduler.currentTime)
    }

    @Test
    fun `empty audio recording flow = BadRecording`() = scope.runTest {
        val webSocket = object : WebSocketBaseFake(this) {
            override fun send(bytes: ByteString): Boolean {
                launch {
                    delay(TEST_RESPONSE_DELAY)
                    outputChannel.send(
                        SocketEvent.MessageReceived(getJson("audd_response_auth_error.json"))
                    )
                }
                return true
            }
        }
        webSocket.openConnection(TEST_SOCKET_OPENING_DELAY)
        val service = AuddRecognitionService(
            config = config,
            webSocketSession = wrapSocketToService(webSocket),
            ioDispatcher = testDispatcher,
            okHttpClient = okHttpClient,
            json = json,
        )
        val delayBeforeFlowClose = 5500L
        val result = service.recognizeFirst(emptyAudioRecordingFlow(delayBeforeFlowClose))
        Assert.assertTrue(result is RemoteRecognitionResult.Error.BadRecording)
        Assert.assertEquals(delayBeforeFlowClose, testScheduler.currentTime)
    }

    private fun getJson(name: String): String {
        return this::class.java.classLoader!!
            .getResourceAsStream(name).bufferedReader().readText()
    }
}

private fun wrapSocketToService(webSocket: WebSocketBaseFake) = object : WebSocketSession {

    override suspend fun startReconnectingSession(url: String): Flow<SocketEvent> = flow {
        emitAll(webSocket.outputChannel)
    }
}
