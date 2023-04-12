package com.mrsep.musicrecognizer.glue.recognitionqueue.adapters

import com.mrsep.musicrecognizer.data.player.PlayerDataController
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.PlayerController
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.model.PlayerStatus
import com.mrsep.musicrecognizer.glue.recognitionqueue.mapper.PlayerStatusToDomainMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject

class AdapterPlayerController @Inject constructor(
    private val playerDataController: PlayerDataController,
    private val statusToDomainMapper: PlayerStatusToDomainMapper
) : PlayerController {

    override val statusFlow: Flow<PlayerStatus>
        get() = playerDataController.statusFlow
            .map { status -> statusToDomainMapper.map(status) }

    override fun start(file: File) {
        playerDataController.start(file)
    }

    override fun stop() {
        playerDataController.stop()
    }

}