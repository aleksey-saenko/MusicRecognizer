package com.mrsep.musicrecognizer.data.player

import kotlinx.coroutines.flow.StateFlow
import java.io.File

interface PlayerDataController {
    val statusFlow: StateFlow<PlayerDataStatus>

    fun start(file: File)
    fun stop()
    fun pause()
    fun resume()
}