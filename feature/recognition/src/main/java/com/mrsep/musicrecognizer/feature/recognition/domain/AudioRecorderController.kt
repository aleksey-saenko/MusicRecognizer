package com.mrsep.musicrecognizer.feature.recognition.domain

import com.mrsep.musicrecognizer.feature.recognition.domain.model.AudioRecordingStrategy
import kotlinx.coroutines.flow.Flow

interface AudioRecorderController {

    val maxAmplitudeFlow: Flow<Float>

    suspend fun audioRecordingFlow(strategy: AudioRecordingStrategy): Flow<Result<ByteArray>>

}