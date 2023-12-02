package com.mrsep.musicrecognizer.feature.recognition.domain

import com.mrsep.musicrecognizer.feature.recognition.domain.model.RemoteRecognitionResult
import kotlinx.coroutines.flow.Flow
import java.io.File

interface RemoteRecognitionService {

    suspend fun recognize(
        token: String,
        audioRecordingFlow: Flow<ByteArray>
    ): RemoteRecognitionResult

    suspend fun recognize(
        token: String,
        file: File
    ): RemoteRecognitionResult

}