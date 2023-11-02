package com.mrsep.musicrecognizer.feature.recognition.domain.model

import java.io.File
import kotlin.time.Duration

sealed class PlayerStatus {

        data object Idle : PlayerStatus()

    data class Started(
        val record: File,
        val duration: Duration
    ) : PlayerStatus()

    data class Paused(
        val record: File,
        val duration: Duration
    ) : PlayerStatus()

    data class Error(
        val record: File,
        val message: String
    ) : PlayerStatus()

}