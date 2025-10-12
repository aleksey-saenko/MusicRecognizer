package com.mrsep.musicrecognizer.core.audio.audioplayer

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

interface PlayerController {

    val statusFlow: Flow<PlayerStatus>
    val playbackPositionMs: StateFlow<Long>

    fun start(id: Int, file: File)
    fun pause()
    fun resume()
    fun stop()
}
