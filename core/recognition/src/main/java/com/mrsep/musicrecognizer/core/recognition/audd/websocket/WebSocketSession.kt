package com.mrsep.musicrecognizer.core.recognition.audd.websocket

import kotlinx.coroutines.flow.Flow

internal interface WebSocketSession {

    suspend fun startReconnectingSession(url: String): Flow<SocketEvent>
}
