package com.mrsep.musicrecognizer.data.recorder

import kotlinx.coroutines.flow.Flow
import java.time.Duration

interface RecorderDataController {
    val maxAmplitudeFlow: Flow<Float>

    suspend fun recordAudioToFile(duration: Duration): RecordDataResult
}