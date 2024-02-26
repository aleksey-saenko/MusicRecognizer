package com.mrsep.musicrecognizer.glue.recognition.adapters

import com.mrsep.musicrecognizer.data.audioplayer.PlayerControllerDo
import com.mrsep.musicrecognizer.feature.recognition.domain.PlayerController
import com.mrsep.musicrecognizer.feature.recognition.domain.model.PlayerStatus
import com.mrsep.musicrecognizer.glue.recognition.mapper.PlayerStatusMapper
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

    override val playbackPositionFlow: Flow<Int>
        get() = playerControllerDo.playbackPositionFlow

    override fun start(id: Int, file: File) {
        playerControllerDo.start(id, file)
    }

    override fun pause() {
        playerControllerDo.pause()
    }

    override fun resume() {
        playerControllerDo.resume()
    }

    override fun stop() {
        playerControllerDo.stop()
    }

}