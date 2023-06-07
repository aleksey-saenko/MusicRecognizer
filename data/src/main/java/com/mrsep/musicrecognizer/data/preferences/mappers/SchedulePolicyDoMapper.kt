package com.mrsep.musicrecognizer.data.preferences.mappers

import com.mrsep.musicrecognizer.UserPreferencesProto.ScheduleActionProto
import com.mrsep.musicrecognizer.UserPreferencesProto.SchedulePolicyProto
import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.data.preferences.ScheduleActionDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo.*
import javax.inject.Inject

class SchedulePolicyDoMapper @Inject constructor(
    private val actionMapper: BidirectionalMapper<ScheduleActionProto, ScheduleActionDo>
): BidirectionalMapper<SchedulePolicyProto, SchedulePolicyDo> {

    override fun map(input: SchedulePolicyProto): SchedulePolicyDo {
        return SchedulePolicyDo(
            noMatches = actionMapper.map(input.noMatches),
            badConnection = actionMapper.map(input.badConnection),
            anotherFailure = actionMapper.map(input.anotherFailure)
        )
    }

    override fun reverseMap(input: SchedulePolicyDo): SchedulePolicyProto {
        return SchedulePolicyProto.newBuilder()
            .setNoMatches(actionMapper.reverseMap(input.noMatches))
            .setBadConnection(actionMapper.reverseMap(input.badConnection))
            .setAnotherFailure(actionMapper.reverseMap(input.anotherFailure))
            .build()
    }

}