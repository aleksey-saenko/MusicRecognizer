package com.mrsep.musicrecognizer.data

import com.mrsep.musicrecognizer.data.remote.audd.websocket.SocketEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import okhttp3.Request
import okhttp3.WebSocket
import okio.ByteString
import java.lang.IllegalStateException

internal abstract class WebSocketBaseFake(
    private val testScope: TestScope
) : WebSocket {

    protected var canceled = false
    protected var closed = false
    protected var queueSize = 0L

    val outputChannel = Channel<SocketEvent>(capacity = 64)

    fun openSocketWithDelay(delayBeforeOpening: Long) {
        val webSocket = this
        testScope.launch {
            delay(delayBeforeOpening)
            outputChannel.trySend(SocketEvent.ConnectionOpened(webSocket))
        }
    }

    fun simulateReconnect(delayBeforeClosing: Long, delayBeforeOpening: Long) {
        testScope.launch {
            delay(delayBeforeClosing)
            outputChannel.trySend(
                SocketEvent.ConnectionFailed(
                    RuntimeException("Web Socket closed by server due to timeout")
                )
            )
            openSocketWithDelay(delayBeforeOpening)
        }
    }

    suspend fun clearQueue(bytes: ByteString, delayToClear: Long) {
        delay(delayToClear)
        queueSize -= bytes.size.toLong()
    }

    override fun cancel() {
        outputChannel.close()
        canceled = true
    }

    override fun close(code: Int, reason: String?): Boolean {
        outputChannel.close()
        closed = true
        return true
    }

    override fun queueSize(): Long {
        return queueSize
    }

    override fun request(): Request {
        throw IllegalStateException("Not required in tests")
    }

    override fun send(text: String): Boolean {
        throw IllegalStateException("Not required in tests")
    }
}
