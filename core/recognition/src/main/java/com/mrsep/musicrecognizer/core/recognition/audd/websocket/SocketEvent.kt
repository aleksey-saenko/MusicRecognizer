package com.mrsep.musicrecognizer.core.recognition.audd.websocket

import okhttp3.WebSocket

internal sealed class SocketEvent {

    data class ConnectionOpened(val webSocket: WebSocket) : SocketEvent()

    data class MessageReceived(val message: String) : SocketEvent()

    data class ConnectionClosing(val shutdownReason: ShutdownReason) : SocketEvent()

    data class ConnectionClosed(val shutdownReason: ShutdownReason) : SocketEvent()

    data class ConnectionFailed(val throwable: Throwable) : SocketEvent()
}

internal data class ShutdownReason(val code: Int, val reason: String)
