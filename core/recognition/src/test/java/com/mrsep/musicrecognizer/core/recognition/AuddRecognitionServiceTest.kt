package com.mrsep.musicrecognizer.core.recognition

import com.mrsep.musicrecognizer.core.domain.preferences.AuddConfig
import com.mrsep.musicrecognizer.core.domain.recognition.AudioSample
import com.mrsep.musicrecognizer.core.domain.recognition.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.core.recognition.audd.AuddRecognitionService
import com.mrsep.musicrecognizer.core.recognition.audd.json.AuddResponseJson
import com.mrsep.musicrecognizer.core.recognition.audd.websocket.WebSocketConnection
import com.mrsep.musicrecognizer.core.recognition.audd.websocket.WebSocketSession
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.HttpRequestBuilder
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import org.junit.ClassRule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.time.Instant
import kotlin.String
import kotlin.math.max
import kotlin.random.Random
import kotlin.random.nextLong
import kotlin.time.Duration.Companion.seconds

private const val TIMEOUT_AFTER_RECORDING_FINISHED = 10_000L
private const val TIMEOUT_WEB_SOCKET_RESPONSE = 15_000L

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
    private val httpClientLazy = dagger.Lazy { HttpClient(OkHttp) }

    companion object {
        @ClassRule
        @JvmField
        val temporaryFolder = TemporaryFolder()
    }

    @Test
    fun `Success on first`() = scope.runTest {
        val webSocketSession = object : WebSocketSession {
            override fun startReconnectingSession(
                maxReconnectAttempts: Int,
                block: HttpRequestBuilder.() -> Unit,
            ): Flow<Result<WebSocketConnection>> = flow {
                delay(TEST_SOCKET_OPENING_DELAY)
                emit(Result.success(
                    object: WebSocketConnection {
                        override val responseChannel = Channel<AuddResponseJson>(Channel.BUFFERED)
                        override suspend fun sendSample(data: ByteArray) {
                            delay(TEST_RESPONSE_DELAY)
                            responseChannel.send(json.decodeFrom(AuddResponse.SUCCESS))
                        }
                    }
                ))
            }
        }
        val service = AuddRecognitionService(
            config = config,
            ioDispatcher = testDispatcher,
            httpClientLazy = httpClientLazy,
            webSocketSession = webSocketSession,
            json = json,
        )
        val result = service.recognizeUntilFirstMatch(normalAudioRecordingFlow(temporaryFolder))
        val expectedTime = TEST_SOCKET_OPENING_DELAY + TEST_RESPONSE_DELAY
        result.shouldBeInstanceOf<RemoteRecognitionResult.Success>()
        testScheduler.currentTime.shouldBe(expectedTime)
    }

    @Test
    fun `NoMatches on single recording = NoMatches`() = scope.runTest {
        val webSocketSession = object : WebSocketSession {
            override fun startReconnectingSession(
                maxReconnectAttempts: Int,
                block: HttpRequestBuilder.() -> Unit,
            ): Flow<Result<WebSocketConnection>> = flow {
                delay(TEST_SOCKET_OPENING_DELAY)
                emit(Result.success(
                    object: WebSocketConnection {
                        override val responseChannel = Channel<AuddResponseJson>(Channel.BUFFERED)
                        override suspend fun sendSample(data: ByteArray) {
                            delay(TEST_RESPONSE_DELAY)
                            responseChannel.send(json.decodeFrom(AuddResponse.NO_MATCHES))
                        }
                    }
                ))
            }
        }
        val service = AuddRecognitionService(
            config = config,
            ioDispatcher = testDispatcher,
            httpClientLazy = httpClientLazy,
            webSocketSession = webSocketSession,
            json = json,
        )
        val result = service.recognizeUntilFirstMatch(singleAudioRecordingFlow(
            initialDelay = 0.seconds,
            temporaryFolder = temporaryFolder
        ))
        val expectedTime = TEST_SOCKET_OPENING_DELAY + TEST_RESPONSE_DELAY
        result.shouldBeInstanceOf<RemoteRecognitionResult.NoMatches>()
        testScheduler.currentTime.shouldBe(expectedTime)
    }

    @Test
    fun `NoMatches on each = NoMatches on last`() = scope.runTest {
        val webSocketSession = object : WebSocketSession {
            override fun startReconnectingSession(
                maxReconnectAttempts: Int,
                block: HttpRequestBuilder.() -> Unit,
            ): Flow<Result<WebSocketConnection>> = flow {
                delay(0)
                emit(Result.success(
                    object: WebSocketConnection {
                        override val responseChannel = Channel<AuddResponseJson>(Channel.BUFFERED)
                        override suspend fun sendSample(data: ByteArray) {
                            delay(TEST_RESPONSE_DELAY)
                            responseChannel.send(json.decodeFrom(AuddResponse.NO_MATCHES))
                        }
                    }
                ))
            }
        }
        val service = AuddRecognitionService(
            config = config,
            ioDispatcher = testDispatcher,
            httpClientLazy = httpClientLazy,
            webSocketSession = webSocketSession,
            json = json,
        )
        val result = service.recognizeUntilFirstMatch(normalAudioRecordingFlow(temporaryFolder))
        val expectedTime = normalAudioFlowDuration + TEST_RESPONSE_DELAY
        result.shouldBeInstanceOf<RemoteRecognitionResult.NoMatches>()
        testScheduler.currentTime.shouldBe(expectedTime)
    }

    @Test
    fun `unrecoverable connection failure = immediate UnhandledError`() = scope.runTest {
        val firstConnectDelay = 500L
        val webSocketSession = object : WebSocketSession {
            override fun startReconnectingSession(
                maxReconnectAttempts: Int,
                block: HttpRequestBuilder.() -> Unit,
            ): Flow<Result<WebSocketConnection>> = flow {
                delay(firstConnectDelay)
                emit(Result.failure(RuntimeException("Protocol error")))
            }
        }
        val service = AuddRecognitionService(
            config = config,
            ioDispatcher = testDispatcher,
            httpClientLazy = httpClientLazy,
            webSocketSession = webSocketSession,
            json = json,
        )
        val result = service.recognizeUntilFirstMatch(normalAudioRecordingFlow(temporaryFolder))
        result.shouldBeInstanceOf<RemoteRecognitionResult.Error.UnhandledError>()
        testScheduler.currentTime.shouldBe(firstConnectDelay)
    }

    @Test
    fun `recoverable connection failure = BadConnection after retry`() = scope.runTest {
        val delayBeforeFinalFailure = 5000L
        val webSocketSession = object : WebSocketSession {
            override fun startReconnectingSession(
                maxReconnectAttempts: Int,
                block: HttpRequestBuilder.() -> Unit,
            ): Flow<Result<WebSocketConnection>> = flow {
                emit(Result.success(
                    object: WebSocketConnection {
                        override val responseChannel = Channel<AuddResponseJson>(Channel.BUFFERED).apply {
                            close()
                        }
                        override suspend fun sendSample(data: ByteArray) {
                            throw ClosedSendChannelException(null)
                        }
                    }
                ))
                delay(delayBeforeFinalFailure)
                emit(Result.failure(kotlinx.io.IOException("Web socket closed by server due to timeout")))
            }
        }
        val service = AuddRecognitionService(
            config = config,
            ioDispatcher = testDispatcher,
            httpClientLazy = httpClientLazy,
            webSocketSession = webSocketSession,
            json = json,
        )
        val result = service.recognizeUntilFirstMatch(normalAudioRecordingFlow(temporaryFolder))
        result.shouldBeInstanceOf<RemoteRecognitionResult.Error.BadConnection>()
        testScheduler.currentTime.shouldBe(delayBeforeFinalFailure)
    }

    @Test
    fun `wrong token = immediate AuthError`() = scope.runTest {
        val webSocketSession = object : WebSocketSession {
            override fun startReconnectingSession(
                maxReconnectAttempts: Int,
                block: HttpRequestBuilder.() -> Unit,
            ): Flow<Result<WebSocketConnection>> = flow {
                delay(TEST_SOCKET_OPENING_DELAY)
                emit(Result.success(
                    object: WebSocketConnection {
                        override val responseChannel = Channel<AuddResponseJson>(Channel.BUFFERED)
                        override suspend fun sendSample(data: ByteArray) {
                            delay(TEST_RESPONSE_DELAY)
                            responseChannel.send(json.decodeFrom(AuddResponse.ERROR_AUTH))
                        }
                    }
                ))
            }
        }
        val service = AuddRecognitionService(
            config = config,
            ioDispatcher = testDispatcher,
            httpClientLazy = httpClientLazy,
            webSocketSession = webSocketSession,
            json = json,
        )
        val result = service.recognizeUntilFirstMatch(normalAudioRecordingFlow(temporaryFolder))
        val expectedTime = TEST_SOCKET_OPENING_DELAY + TEST_RESPONSE_DELAY
        result.shouldBeInstanceOf<RemoteRecognitionResult.Error.AuthError>()
        testScheduler.currentTime.shouldBe(expectedTime)
    }

    @Test
    fun `TIMEOUT_AFTER_RECORDING_FINISHED reached = last result`() = scope.runTest {
        val webSocketSession = object : WebSocketSession {
            var firstMessage = true
            override fun startReconnectingSession(
                maxReconnectAttempts: Int,
                block: HttpRequestBuilder.() -> Unit,
            ): Flow<Result<WebSocketConnection>> = flow {
                emit(Result.success(
                    object: WebSocketConnection {
                        override val responseChannel = Channel<AuddResponseJson>(Channel.BUFFERED)
                        override suspend fun sendSample(data: ByteArray) {
                            if (firstMessage) {
                                delay(5_000)
                            } else {
                                delay(10_000)
                            }
                            delay(TEST_RESPONSE_DELAY)
                            responseChannel.send(json.decodeFrom(AuddResponse.NO_MATCHES))
                        }
                    }
                ))
            }
        }
        val service = AuddRecognitionService(
            config = config,
            ioDispatcher = testDispatcher,
            httpClientLazy = httpClientLazy,
            webSocketSession = webSocketSession,
            json = json,
        )
        val result = service.recognizeUntilFirstMatch(normalAudioRecordingFlow(temporaryFolder))
        val expectedTime = normalAudioFlowDuration + TIMEOUT_AFTER_RECORDING_FINISHED
        result.shouldBeInstanceOf<RemoteRecognitionResult.NoMatches>()
        testScheduler.currentTime.shouldBe(expectedTime)
    }

    @Test
    fun `empty audio recording flow = BadRecording`() = scope.runTest {
        val webSocketSession = object : WebSocketSession {
            override fun startReconnectingSession(
                maxReconnectAttempts: Int,
                block: HttpRequestBuilder.() -> Unit,
            ): Flow<Result<WebSocketConnection>> = flow {
                delay(Long.MAX_VALUE)
            }
        }
        val service = AuddRecognitionService(
            config = config,
            ioDispatcher = testDispatcher,
            httpClientLazy = httpClientLazy,
            webSocketSession = webSocketSession,
            json = json,
        )
        val delayBeforeFlowClose = 5500L
        val result = service.recognizeUntilFirstMatch(emptyAudioRecordingFlow(delayBeforeFlowClose))
        result.shouldBeInstanceOf<RemoteRecognitionResult.Error.BadRecording>()
        testScheduler.currentTime.shouldBe(delayBeforeFlowClose)
    }

    @Test
    fun `no response within TIMEOUT_WEB_SOCKET_RESPONSE = BadConnection`() = scope.runTest {
        val webSocketSession = object : WebSocketSession {
            override fun startReconnectingSession(
                maxReconnectAttempts: Int,
                block: HttpRequestBuilder.() -> Unit,
            ): Flow<Result<WebSocketConnection>> = flow {
                delay(TEST_SOCKET_OPENING_DELAY)
                emit(Result.success(
                    object: WebSocketConnection {
                        var recordingReceived = false
                        override val responseChannel = Channel<AuddResponseJson>(Channel.BUFFERED)
                        override suspend fun sendSample(data: ByteArray) {
                            recordingReceived.shouldBeFalse()
                            recordingReceived = true
                        }
                    }
                ))
            }
        }
        val service = AuddRecognitionService(
            config = config,
            ioDispatcher = testDispatcher,
            httpClientLazy = httpClientLazy,
            webSocketSession = webSocketSession,
            json = json,
        )
        val result = service.recognizeUntilFirstMatch(infinityAudioRecordingFlow(
            interval = 3.seconds,
            initialDelay = 0.seconds,
            temporaryFolder = temporaryFolder
        ))
        val expectedTime = TEST_SOCKET_OPENING_DELAY + TIMEOUT_WEB_SOCKET_RESPONSE
        result.shouldBeInstanceOf<RemoteRecognitionResult.Error.BadConnection>()
        testScheduler.currentTime.shouldBe(expectedTime)
    }

    @Test
    fun `NoMatches on first rec, BadConnection on second = Success on second after reconnect`() = scope.runTest {
        val reconnectionDelay = Random.nextLong(normalAudioDelay3 + 100L..normalAudioFlowDuration)
        val webSocketSession = object : WebSocketSession {
            override fun startReconnectingSession(
                maxReconnectAttempts: Int,
                block: HttpRequestBuilder.() -> Unit,
            ): Flow<Result<WebSocketConnection>> = flow {
                var recordingCount = 0
                val reconnectTrigger = CompletableDeferred<Unit>()
                var unprocessedRecording: ByteArray? = null

                emit(Result.success(
                    object: WebSocketConnection {
                        override val responseChannel = Channel<AuddResponseJson>(Channel.BUFFERED)
                        override suspend fun sendSample(data: ByteArray) {
                            recordingCount++
                            when (recordingCount) {
                                1 -> {
                                    delay(TEST_RESPONSE_DELAY)
                                    responseChannel.send(json.decodeFrom(AuddResponse.NO_MATCHES))
                                }
                                2 -> {
                                    delay(TEST_RESPONSE_DELAY)
                                    responseChannel.close()
                                    unprocessedRecording = data
                                    reconnectTrigger.complete(Unit)
                                }
                                else -> throw ClosedSendChannelException(null)
                            }
                        }
                    }
                ))

                reconnectTrigger.await()
                delay(reconnectionDelay)

                emit(Result.success(
                    object: WebSocketConnection {
                        override val responseChannel = Channel<AuddResponseJson>(Channel.BUFFERED)
                        override suspend fun sendSample(data: ByteArray) {
                            unprocessedRecording.shouldBe(data)
                            recordingCount.shouldBe(2)
                            delay(TEST_RESPONSE_DELAY)
                            responseChannel.send(json.decodeFrom(AuddResponse.SUCCESS))
                        }
                    }
                ))
            }
        }
        val service = AuddRecognitionService(
            config = config,
            ioDispatcher = testDispatcher,
            httpClientLazy = httpClientLazy,
            webSocketSession = webSocketSession,
            json = json,
        )
        val result = service.recognizeUntilFirstMatch(normalAudioRecordingFlow(temporaryFolder))
        val firstResultTime = normalAudioDelay1 + TEST_RESPONSE_DELAY
        val failureTime = max(firstResultTime, normalAudioDelay2) + TEST_RESPONSE_DELAY
        val expectedTime = failureTime + reconnectionDelay + TEST_RESPONSE_DELAY
        result.shouldBeInstanceOf<RemoteRecognitionResult.Success>()
        testScheduler.currentTime.shouldBe(expectedTime)
    }

    @Test
    fun `Bad audio recording file = BadRecording`() = scope.runTest {
        val webSocketSession = object : WebSocketSession {
            override fun startReconnectingSession(
                maxReconnectAttempts: Int,
                block: HttpRequestBuilder.() -> Unit,
            ): Flow<Result<WebSocketConnection>> = flow {
                emit(Result.success(
                    object: WebSocketConnection {
                        override val responseChannel = Channel<AuddResponseJson>(Channel.BUFFERED)
                        override suspend fun sendSample(data: ByteArray) {}
                    }
                ))
            }
        }
        val service = AuddRecognitionService(
            config = config,
            ioDispatcher = testDispatcher,
            httpClientLazy = httpClientLazy,
            webSocketSession = webSocketSession,
            json = json,
        )
        val delayBeforeSampleRead = 40L
        val result = service.recognizeUntilFirstMatch(flow {
            delay(delayBeforeSampleRead)
            emit(AudioSample(
                file = File("not_exist"),
                timestamp = Instant.now(),
                duration = 3.seconds,
                sampleRate = 48_000,
                mimeType = "audio/mp4",
            ))
        })
        val expectedTime = delayBeforeSampleRead
        result.shouldBeInstanceOf<RemoteRecognitionResult.Error.BadRecording>()
        result.cause.shouldBeInstanceOf<IOException>()
        testScheduler.currentTime.shouldBe(expectedTime)
    }
}

private enum class AuddResponse {
    SUCCESS,
    NO_MATCHES,
    ERROR_AUTH,
    ERROR_RECORDING;

    fun asJsonString(): String {
        val name = when (this) {
            SUCCESS -> "audd_response_success.json"
            NO_MATCHES -> "audd_response_no_matches.json"
            ERROR_AUTH -> "audd_response_auth_error.json"
            ERROR_RECORDING -> "audd_response_recording_error.json"
        }
        val stream = javaClass.classLoader!!.getResourceAsStream(name)
            ?: error("Test resource not found: $name. Put it under src/test/resources/")
        return stream.bufferedReader().use { it.readText() }
    }
}

private fun Json.decodeFrom(response: AuddResponse): AuddResponseJson {
    return decodeFromString(response.asJsonString())
}
