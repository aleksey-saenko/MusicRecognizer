package com.mrsep.musicrecognizer.data.audioplayer

import java.io.File
import kotlin.time.Duration

sealed class PlayerStatusDo {

    data object Idle : PlayerStatusDo()

    data class Started(
        val id: Int,
        val record: File,
        val duration: Duration
    ) : PlayerStatusDo()

    data class Paused(
        val id: Int,
        val record: File,
        val duration: Duration
    ) : PlayerStatusDo()

    data class Error(
        val id: Int,
        val record: File,
        val message: String
    ) : PlayerStatusDo()

}