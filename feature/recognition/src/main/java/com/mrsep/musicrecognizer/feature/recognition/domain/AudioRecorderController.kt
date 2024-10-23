package com.mrsep.musicrecognizer.feature.recognition.domain

import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionScheme
import kotlinx.coroutines.flow.Flow

interface AudioRecorderController {

    val soundLevel: Flow<Float>

    suspend fun audioRecordingFlow(scheme: RecognitionScheme): Flow<Result<ByteArray>>
}
