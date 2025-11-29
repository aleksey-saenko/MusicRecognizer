package com.mrsep.musicrecognizer.core.domain.recognition

import com.mrsep.musicrecognizer.core.domain.recognition.model.RecordingScheme
import com.mrsep.musicrecognizer.core.domain.recognition.model.RemoteRecognitionResult
import kotlinx.coroutines.flow.Flow

interface RemoteRecognitionService {

    val recordingScheme: RecordingScheme

    suspend fun recognize(sample: AudioSample): RemoteRecognitionResult

    suspend fun recognizeUntilFirstMatch(samples: Flow<AudioSample>): RemoteRecognitionResult
}
