package com.mrsep.musicrecognizer.core.domain.recognition

import com.mrsep.musicrecognizer.core.domain.recognition.model.RemoteRecognitionResult
import kotlinx.coroutines.flow.Flow
import java.io.File

interface RemoteRecognitionService {

    suspend fun recognize(recording: ByteArray): RemoteRecognitionResult

    suspend fun recognize(recordingFlow: Flow<ByteArray>): RemoteRecognitionResult

    suspend fun recognize(recording: File): RemoteRecognitionResult

}
