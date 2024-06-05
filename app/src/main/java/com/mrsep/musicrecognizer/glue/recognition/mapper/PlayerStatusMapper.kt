package com.mrsep.musicrecognizer.glue.recognition.mapper

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.audioplayer.PlayerStatusDo
import com.mrsep.musicrecognizer.feature.recognition.domain.model.PlayerStatus
import javax.inject.Inject

class PlayerStatusMapper @Inject constructor() : Mapper<PlayerStatusDo, PlayerStatus> {

    override fun map(input: PlayerStatusDo): PlayerStatus {
        return when (input) {
            PlayerStatusDo.Idle -> PlayerStatus.Idle
            is PlayerStatusDo.Paused -> PlayerStatus.Paused(input.id, input.record, input.duration)
            is PlayerStatusDo.Started -> PlayerStatus.Started(input.id, input.record, input.duration)
            is PlayerStatusDo.Error -> PlayerStatus.Error(input.id, input.record, input.message)
        }
    }
}
