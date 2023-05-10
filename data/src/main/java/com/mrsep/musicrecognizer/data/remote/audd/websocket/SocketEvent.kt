package com.mrsep.musicrecognizer.data.remote.audd.websocket

import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionDataResult
import okhttp3.WebSocket

sealed class SocketEvent {

    data class ConnectionOpened(val webSocket: WebSocket) : SocketEvent()

    data class ResponseReceived(val response: RemoteRecognitionDataResult) : SocketEvent()

    data class ConnectionClosing(val shutdownReason: ShutdownReason) : SocketEvent()

    data class ConnectionClosed(val shutdownReason: ShutdownReason) : SocketEvent()

    data class ConnectionFailed(val throwable: Throwable) : SocketEvent()

}

data class ShutdownReason(val code: Int, val reason: String)