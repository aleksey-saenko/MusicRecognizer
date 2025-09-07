package com.mrsep.musicrecognizer.core.audio.audioplayer

import kotlinx.coroutines.flow.Flow
import java.io.File

interface PlayerController {

    val statusFlow: Flow<PlayerStatus>
    val playbackPositionFlow: Flow<Int>

    fun start(id: Int, file: File)
    fun pause()
    fun resume()
    fun stop()
}
