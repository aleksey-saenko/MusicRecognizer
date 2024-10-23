package com.mrsep.musicrecognizer.data.audiorecord

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

interface AudioRecordingControllerDo {

    val soundLevel: StateFlow<Float>

    fun audioRecordingFlow(scheme: RecognitionSchemeDo): Flow<Result<AudioRecordingDo>>
}

class AudioRecordingDo(
    val data: ByteArray,
    val duration: Duration,
    val nonSilenceDuration: Duration,
)
