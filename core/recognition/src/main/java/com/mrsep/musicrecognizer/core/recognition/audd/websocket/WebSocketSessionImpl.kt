package com.mrsep.musicrecognizer.core.recognition.audd.websocket

import com.mrsep.musicrecognizer.core.recognition.audd.json.AuddResponseJson
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttpEngine
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.websocket.send
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retryWhen
import kotlinx.io.IOException
import javax.inject.Inject
import kotlin.math.pow
import kotlin.random.Random

internal class WebSocketSessionImpl @Inject constructor(
    private val httpClientLazy: dagger.Lazy<HttpClient>,
) : WebSocketSession {

    @OptIn(DelicateCoroutinesApi::class)
    override fun startReconnectingSession(
        maxReconnectAttempts: Int,
        block: HttpRequestBuilder.() -> Unit
    ): Flow<Result<WebSocketConnection>> = flow {
        val httpClient = httpClientLazy.get()
        val session = httpClient.webSocketSession(block)
        val responseChannel = Channel<AuddResponseJson>(Channel.BUFFERED)
        val connection = object : WebSocketConnection {
            override suspend fun sendRecording(data: ByteArray) = session.send(data)
            override val responseChannel = responseChannel
        }
        emit(Result.success(connection))
        try {
            while (true) {
                val response = session.receiveDeserialized<AuddResponseJson>()
                responseChannel.send(response)
            }
        } catch (_: ClosedReceiveChannelException) {
            val closeReason = session.closeReason.await()
            throw IOException(
                "Web socket session is closed by server. " +
                        (closeReason?.let { "${it.knownReason}." } ?: "Unknown reason.")
            )
        } finally {
            session.cancel()
            responseChannel.close()
        }
    }.retryWhen { cause, attempt ->
        val engine = httpClientLazy.get().engine
        if (isRecoverableWebSocketFailure(cause, engine) && attempt < maxReconnectAttempts) {
            val expDelay = minOf((2.0.pow(attempt.toInt()) * BASE_RETRY_DELAY).toLong(), MAX_RETRY_DELAY)
            val jitter = Random.nextLong(0, JITTER_MAX_DELAY)
            delay(expDelay + jitter)
            true
        } else {
            emit(Result.failure(cause))
            false
        }
    }

    companion object {
        private const val BASE_RETRY_DELAY = 1000L
        private const val MAX_RETRY_DELAY = 5000L
        private const val JITTER_MAX_DELAY = 1000L
    }
}

// See okhttp RetryAndFollowUpInterceptor and RealWebSocket
internal fun isRecoverableWebSocketFailure(
    throwable: Throwable,
    engine: HttpClientEngine,
): Boolean {
    check(engine is OkHttpEngine) { error("Unknow HttpClientEngine") }
    return when (throwable) {
        is java.net.ProtocolException -> false
        is javax.net.ssl.SSLHandshakeException -> throwable.cause !is java.security.cert.CertificateException
        is javax.net.ssl.SSLPeerUnverifiedException -> false
        // SocketTimeoutException is used on missing pong frame
        is java.io.InterruptedIOException -> throwable is java.net.SocketTimeoutException

        is IOException -> true
        else -> false
    }
}
