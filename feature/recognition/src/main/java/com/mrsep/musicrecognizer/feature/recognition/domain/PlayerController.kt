package com.mrsep.musicrecognizer.feature.recognition.domain

import com.mrsep.musicrecognizer.feature.recognition.domain.model.PlayerStatus
import kotlinx.coroutines.flow.Flow
import java.io.File

interface PlayerController {

    val statusFlow: Flow<PlayerStatus>

    fun start(file: File)
    fun stop()

}