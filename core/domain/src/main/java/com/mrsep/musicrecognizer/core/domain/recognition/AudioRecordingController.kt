package com.mrsep.musicrecognizer.core.domain.recognition

import com.mrsep.musicrecognizer.core.domain.recognition.model.RecordingScheme
import kotlinx.coroutines.flow.Flow

interface AudioRecordingController {

    val soundLevel: Flow<Float>

    fun audioRecordingFlow(scheme: RecordingScheme): Flow<Result<AudioRecording>>
}
