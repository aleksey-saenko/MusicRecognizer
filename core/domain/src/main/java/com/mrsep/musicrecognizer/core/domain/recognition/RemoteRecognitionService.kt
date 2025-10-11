package com.mrsep.musicrecognizer.core.domain.recognition

import com.mrsep.musicrecognizer.core.domain.recognition.model.RemoteRecognitionResult
import kotlinx.coroutines.flow.Flow

interface RemoteRecognitionService {

    suspend fun recognize(sample: AudioSample): RemoteRecognitionResult

    suspend fun recognizeUntilFirstMatch(samples: Flow<AudioSample>): RemoteRecognitionResult
}
