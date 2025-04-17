package com.mrsep.musicrecognizer.core.recognition.audd.websocket

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject

internal class WebSocketSessionImpl @Inject constructor(
    private val okHttpClient: OkHttpClient,
) : WebSocketSession {

    override suspend fun startReconnectingSession(url: String): Flow<SocketEvent> = flow {
        var reconnectionDelay = 1000L
        while (true) {
            emitAll(startNewSession(url))
            delay(reconnectionDelay)
            if (reconnectionDelay < 4000L) reconnectionDelay *= 2
        }
    }

    private fun startNewSession(url: String): Flow<SocketEvent> = callbackFlow {
        val eventsListener = object : WebSocketListener() {
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                trySendBlocking(SocketEvent.ConnectionClosed(ShutdownReason(code, reason)))
                close()
            }
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                trySendBlocking(SocketEvent.ConnectionClosing(ShutdownReason(code, reason)))
            }
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                trySendBlocking(SocketEvent.ConnectionFailed(t))
                close()
            }
            override fun onMessage(webSocket: WebSocket, text: String) {
                trySendBlocking(SocketEvent.MessageReceived(text))
            }
            override fun onOpen(webSocket: WebSocket, response: Response) {
                trySendBlocking(SocketEvent.ConnectionOpened(webSocket))
            }
        }
        val webSocket = okHttpClient.newWebSocket(
            Request.Builder().url(url).build(),
            eventsListener
        )
        awaitClose {
            webSocket.cancel()
        }
    }
}
