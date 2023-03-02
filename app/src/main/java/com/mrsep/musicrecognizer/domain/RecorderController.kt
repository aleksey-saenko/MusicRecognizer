package com.mrsep.musicrecognizer.domain

import java.io.File
import java.time.Duration

interface RecorderController {

    suspend fun recordAudioToFile(file: File, duration: Duration): RecordResult

}

sealed interface RecordResult {
    object Success : RecordResult
    sealed interface Error : RecordResult {
        object PrepareFailed : Error
        object ServerDied : Error
        data class UnhandledError(val throwable: Throwable? = null) : Error
    }
}