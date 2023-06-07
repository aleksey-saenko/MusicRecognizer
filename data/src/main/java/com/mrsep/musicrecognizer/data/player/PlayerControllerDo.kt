package com.mrsep.musicrecognizer.data.player

import kotlinx.coroutines.flow.StateFlow
import java.io.File

interface PlayerControllerDo {
    val statusFlow: StateFlow<PlayerStatusDo>

    fun start(file: File)
    fun stop()
    fun pause()
    fun resume()
}