package com.mrsep.musicrecognizer.domain

import java.io.File
import java.time.Duration

interface RecorderController {

//    val statusFlow: Flow<RecorderStatus>

    suspend fun recordAudioToFile(file: File, duration: Duration): RecordResult

}

sealed interface RecordResult {
    object Success : RecordResult
    sealed interface Error : RecordResult {
        object PrepareFailed : Error
        object Unknown : Error
        object ServerDied : Error
    }
}

//sealed class RecorderStatus {
//    object Ready : RecorderStatus()
//    object Recording : RecorderStatus()
//    object Stopped : RecorderStatus()
//    object Failure : RecorderStatus()
//}