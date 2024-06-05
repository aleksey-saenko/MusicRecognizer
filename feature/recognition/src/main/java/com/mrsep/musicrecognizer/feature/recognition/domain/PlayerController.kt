package com.mrsep.musicrecognizer.feature.recognition.domain

import com.mrsep.musicrecognizer.feature.recognition.domain.model.PlayerStatus
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
