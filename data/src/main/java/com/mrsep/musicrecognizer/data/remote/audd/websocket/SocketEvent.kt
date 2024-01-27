package com.mrsep.musicrecognizer.data.remote.audd.websocket

import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionResultDo
import okhttp3.WebSocket

internal sealed class SocketEvent {

    data class ConnectionOpened(val webSocket: WebSocket) : SocketEvent()

    data class ResponseReceived(val response: RemoteRecognitionResultDo) : SocketEvent()

    data class ConnectionClosing(val shutdownReason: ShutdownReason) : SocketEvent()

    data class ConnectionClosed(val shutdownReason: ShutdownReason) : SocketEvent()

    data class ConnectionFailed(val throwable: Throwable) : SocketEvent()

}

internal data class ShutdownReason(val code: Int, val reason: String)