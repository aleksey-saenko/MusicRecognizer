package com.mrsep.musicrecognizer.data.remote.audd.websocket

import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionDataResult
import kotlinx.coroutines.flow.Flow

interface RecognitionStreamDataService {

    suspend fun recognize(
        token: String,
        requiredServices: UserPreferencesProto.RequiredServicesProto,
        audioRecordingFlow: Flow<ByteArray>
    ): RemoteRecognitionDataResult

}