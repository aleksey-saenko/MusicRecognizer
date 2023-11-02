package com.mrsep.musicrecognizer.data.player

import kotlinx.coroutines.flow.Flow
import java.io.File

interface PlayerControllerDo {
    val statusFlow: Flow<PlayerStatusDo>
    val playbackPositionFlow: Flow<Int>

    fun start(file: File)
    fun stop()
    fun pause()
    fun resume()
}