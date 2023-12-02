package com.mrsep.musicrecognizer.data.remote.audd.websocket

import kotlinx.coroutines.flow.Flow

interface AuddRecognitionWebSocketService {

    suspend fun startSession(token: String): Flow<SocketEvent>

}