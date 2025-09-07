package com.mrsep.musicrecognizer.core.domain.recognition

import com.mrsep.musicrecognizer.core.domain.recognition.model.RemoteRecognitionResult
import kotlinx.coroutines.flow.Flow

interface RemoteRecognitionService {

    suspend fun recognize(recording: AudioRecording): RemoteRecognitionResult

    suspend fun recognizeFirst(recordingFlow: Flow<AudioRecording>): RemoteRecognitionResult
}
