package com.mrsep.musicrecognizer.domain

import java.io.File

interface RecorderController {

//    val statusFlow: Flow<RecorderStatus>
    val recordFile: File

    fun startRecord()
    fun stopRecord()

}

sealed class RecorderStatus {
    object Ready : RecorderStatus()
    object Recording : RecorderStatus()
    object Stopped : RecorderStatus()
    object Failure : RecorderStatus()
}