package com.mrsep.musicrecognizer.glue.preferences.mapper

import com.mrsep.musicrecognizer.UserPreferencesProto.ScheduleActionProto
import com.mrsep.musicrecognizer.UserPreferencesProto.SchedulePolicyProto
import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.feature.preferences.domain.ScheduleAction
import com.mrsep.musicrecognizer.feature.preferences.domain.UserPreferences.SchedulePolicy
import javax.inject.Inject

class SchedulePolicyMapper @Inject constructor(
    private val actionMapper: BidirectionalMapper<ScheduleActionProto, ScheduleAction>
): BidirectionalMapper<SchedulePolicyProto, SchedulePolicy> {

    override fun map(input: SchedulePolicyProto): SchedulePolicy {
        return SchedulePolicy(
            noMatches = actionMapper.map(input.noMatches),
            badConnection = actionMapper.map(input.badConnection),
            anotherFailure = actionMapper.map(input.anotherFailure)
        )
    }

    override fun reverseMap(input: SchedulePolicy): SchedulePolicyProto {
        return SchedulePolicyProto.newBuilder()
            .setNoMatches(actionMapper.reverseMap(input.noMatches))
            .setBadConnection(actionMapper.reverseMap(input.badConnection))
            .setAnotherFailure(actionMapper.reverseMap(input.anotherFailure))
            .build()
    }

}