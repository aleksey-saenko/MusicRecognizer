package com.mrsep.musicrecognizer.glue.recognitionqueue.mapper

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.player.PlayerDataStatus
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.model.PlayerStatus
import javax.inject.Inject

class PlayerStatusMapper @Inject constructor() : Mapper<PlayerDataStatus, PlayerStatus> {

    override fun map(input: PlayerDataStatus): PlayerStatus {
        return when (input) {
            PlayerDataStatus.Idle -> PlayerStatus.Idle
            is PlayerDataStatus.Paused -> PlayerStatus.Paused(input.record)
            is PlayerDataStatus.Started -> PlayerStatus.Started(input.record)
            is PlayerDataStatus.Error -> PlayerStatus.Error(input.record, input.message)
        }
    }

}