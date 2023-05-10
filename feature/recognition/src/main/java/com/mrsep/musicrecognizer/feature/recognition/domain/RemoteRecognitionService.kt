package com.mrsep.musicrecognizer.feature.recognition.domain

import com.mrsep.musicrecognizer.feature.recognition.domain.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface RemoteRecognitionService {

    suspend fun recognize(
        token: String,
        requiredServices: UserPreferences.RequiredServices,
        audioRecordingFlow: Flow<ByteArray>
    ): RemoteRecognitionResult

}