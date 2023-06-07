package com.mrsep.musicrecognizer.data.remote.audd.websocket

import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import kotlinx.coroutines.flow.Flow

interface AuddRecognitionWebSocketService {

    suspend fun startSession(
        token: String,
        requiredServices: UserPreferencesDo.RequiredServicesDo
    ): Flow<SocketEvent>

}