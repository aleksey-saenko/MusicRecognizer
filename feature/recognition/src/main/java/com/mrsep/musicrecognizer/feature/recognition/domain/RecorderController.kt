package com.mrsep.musicrecognizer.feature.recognition.domain

import kotlinx.coroutines.flow.Flow
import java.io.File
import java.time.Duration

interface RecorderController {

    val maxAmplitudeFlow: Flow<Float>
    suspend fun recordAudioToFile(duration: Duration): RecordResult

}

sealed class RecordResult {

    data class Success(val file: File) : RecordResult()
    data class Error(val throwable: Throwable) : RecordResult()

}