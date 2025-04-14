package com.mrsep.musicrecognizer.core.recognition

import com.mrsep.musicrecognizer.core.recognition.audd.websocket.SocketEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import okhttp3.Request
import okhttp3.WebSocket
import java.io.IOException
import java.lang.IllegalStateException

internal abstract class WebSocketBaseFake(
    private val testScope: TestScope
) : WebSocket {

    private var canceled = false
    private var closed = false

    override fun queueSize() = 0L

    val outputChannel = Channel<SocketEvent>(Channel.UNLIMITED)

    fun openConnection(delayBeforeOpening: Long) {
        check(!canceled && !closed)
        val webSocket = this
        testScope.launch {
            delay(delayBeforeOpening)
            outputChannel.trySend(SocketEvent.ConnectionOpened(webSocket))
        }
    }

    fun failureConnection(delayBeforeFailure: Long, recoverable: Boolean) {
        check(!canceled && !closed)
        testScope.launch {
            delay(delayBeforeFailure)
            outputChannel.trySend(
                SocketEvent.ConnectionFailed(
                    if (recoverable) {
                        IOException("Web socket closed by server due to timeout")
                    } else {
                        RuntimeException("Protocol error")
                    }
                )
            )
        }
    }

    fun simulateReconnect(delayBeforeClosing: Long, delayBeforeOpening: Long) {
        check(!canceled && !closed)
        testScope.launch {
            delay(delayBeforeClosing)
            outputChannel.trySend(
                SocketEvent.ConnectionFailed(
                    IOException("Web socket closed by server due to timeout")
                )
            )
            delay(delayBeforeOpening)
            outputChannel.trySend(SocketEvent.ConnectionOpened(this@WebSocketBaseFake))
        }
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

    override fun request(): Request {
        check(!canceled && !closed)
        throw IllegalStateException("Not required in tests")
    }

    override fun send(text: String): Boolean {
        check(!canceled && !closed)
        return true
    }
}
