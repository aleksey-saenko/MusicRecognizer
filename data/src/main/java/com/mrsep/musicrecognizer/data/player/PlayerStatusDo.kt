package com.mrsep.musicrecognizer.data.player

import java.io.File

sealed class PlayerStatusDo {

    data object Idle : PlayerStatusDo()
    data class Started(val record: File) : PlayerStatusDo()
    data class Paused(val record: File) : PlayerStatusDo()
    data class Error(val record: File, val message: String) : PlayerStatusDo()

}