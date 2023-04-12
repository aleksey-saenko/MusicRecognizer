package com.mrsep.musicrecognizer.feature.recognitionqueue.domain

import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.model.PlayerStatus
import kotlinx.coroutines.flow.Flow
import java.io.File

interface PlayerController {

    val statusFlow: Flow<PlayerStatus>

    fun start(file: File)
    fun stop()

}