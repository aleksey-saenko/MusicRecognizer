package com.mrsep.musicrecognizer.data.player

import java.io.File

sealed class PlayerDataStatus {

    object Idle : PlayerDataStatus()
    data class Started(val record: File) : PlayerDataStatus()
    data class Paused(val record: File) : PlayerDataStatus()
    data class Error(val record: File, val message: String) : PlayerDataStatus()

}