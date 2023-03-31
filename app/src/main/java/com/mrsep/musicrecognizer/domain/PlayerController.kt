package com.mrsep.musicrecognizer.domain

import kotlinx.coroutines.flow.StateFlow
import java.io.File

interface PlayerController {

    val statusFlow: StateFlow<PlayerStatus>

    fun start(file: File)
    fun stop()
    fun pause()
    fun resume()

}

sealed class PlayerStatus {

    object Idle : PlayerStatus()
    data class Started(val record: File) : PlayerStatus()
    data class Paused(val record: File) : PlayerStatus()
    data class Error(val record: File, val message: String) : PlayerStatus()

}