package com.mrsep.musicrecognizer.data.audioplayer

import kotlinx.coroutines.flow.Flow
import java.io.File

interface PlayerControllerDo {
    val statusFlow: Flow<PlayerStatusDo>
    val playbackPositionFlow: Flow<Int>

    fun start(id: Int, file: File)
    fun stop()
    fun pause()
    fun resume()
}