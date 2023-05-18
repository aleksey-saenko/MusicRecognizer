package com.mrsep.musicrecognizer.data.remote.audd.websocket

import com.mrsep.musicrecognizer.UserPreferencesProto
import kotlinx.coroutines.flow.Flow

interface AuddRecognitionWebSocketService {

    suspend fun startSession(
        token: String,
        requiredServices: UserPreferencesProto.RequiredServicesProto
    ): Flow<SocketEvent>

}