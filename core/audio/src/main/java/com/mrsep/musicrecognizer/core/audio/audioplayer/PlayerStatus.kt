package com.mrsep.musicrecognizer.core.audio.audioplayer

import java.io.File
import kotlin.time.Duration

sealed class PlayerStatus {

    data object Idle : PlayerStatus()

    data class Started(
        val id: Int,
        val record: File,
        val duration: Duration
    ) : PlayerStatus()

    data class Paused(
        val id: Int,
        val record: File,
        val duration: Duration
    ) : PlayerStatus()

    data class Error(
        val id: Int,
        val record: File,
        val message: String
    ) : PlayerStatus()
}
