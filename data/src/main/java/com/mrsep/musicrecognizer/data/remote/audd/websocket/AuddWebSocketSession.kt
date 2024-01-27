package com.mrsep.musicrecognizer.data.remote.audd.websocket

import kotlinx.coroutines.flow.Flow

internal interface AuddWebSocketSession {

    suspend fun startSession(apiToken: String): Flow<SocketEvent>

}