package com.mrsep.musicrecognizer.glue.recognitionqueue.adapters

import com.mrsep.musicrecognizer.data.player.PlayerControllerDo
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.PlayerController
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.model.PlayerStatus
import com.mrsep.musicrecognizer.glue.recognitionqueue.mapper.PlayerStatusMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject

class AdapterPlayerController @Inject constructor(
    private val playerControllerDo: PlayerControllerDo,
    private val statusMapper: PlayerStatusMapper
) : PlayerController {

    override val statusFlow: Flow<PlayerStatus>
        get() = playerControllerDo.statusFlow
            .map { status -> statusMapper.map(status) }

    override fun start(file: File) {
        playerControllerDo.start(file)
    }

    override fun stop() {
        playerControllerDo.stop()
    }

}