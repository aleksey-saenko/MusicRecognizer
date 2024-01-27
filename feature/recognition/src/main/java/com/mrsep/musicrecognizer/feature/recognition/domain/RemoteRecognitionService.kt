package com.mrsep.musicrecognizer.feature.recognition.domain

import com.mrsep.musicrecognizer.feature.recognition.domain.model.RemoteRecognitionResult
import kotlinx.coroutines.flow.Flow
import java.io.File

interface RemoteRecognitionService {

    suspend fun recognize(audioRecordingFlow: Flow<ByteArray>): RemoteRecognitionResult

    suspend fun recognize(file: File): RemoteRecognitionResult

}