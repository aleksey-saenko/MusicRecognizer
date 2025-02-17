package com.mrsep.musicrecognizer.core.recognition.audd.websocket

import kotlinx.coroutines.flow.Flow

internal interface AuddWebSocketSession {

    suspend fun startSession(apiToken: String): Flow<SocketEvent>
}
