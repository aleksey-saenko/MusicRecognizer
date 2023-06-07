package com.mrsep.musicrecognizer.glue.recognitionqueue.mapper

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.player.PlayerStatusDo
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.model.PlayerStatus
import javax.inject.Inject

class PlayerStatusMapper @Inject constructor() : Mapper<PlayerStatusDo, PlayerStatus> {

    override fun map(input: PlayerStatusDo): PlayerStatus {
        return when (input) {
            PlayerStatusDo.Idle -> PlayerStatus.Idle
            is PlayerStatusDo.Paused -> PlayerStatus.Paused(input.record)
            is PlayerStatusDo.Started -> PlayerStatus.Started(input.record)
            is PlayerStatusDo.Error -> PlayerStatus.Error(input.record, input.message)
        }
    }

}