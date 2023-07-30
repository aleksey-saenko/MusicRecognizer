package com.mrsep.musicrecognizer.feature.recognition.domain.model

import java.io.File

sealed class PlayerStatus {

    data object Idle : PlayerStatus()
    data class Started(val record: File) : PlayerStatus()
    data class Paused(val record: File) : PlayerStatus()
    data class Error(val record: File, val message: String) : PlayerStatus()

}