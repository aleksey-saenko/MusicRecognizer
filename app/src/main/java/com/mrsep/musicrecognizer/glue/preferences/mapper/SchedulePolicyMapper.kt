package com.mrsep.musicrecognizer.glue.preferences.mapper

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.data.preferences.ScheduleActionDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo.SchedulePolicyDo
import com.mrsep.musicrecognizer.feature.preferences.domain.ScheduleAction
import com.mrsep.musicrecognizer.feature.preferences.domain.UserPreferences.SchedulePolicy
import javax.inject.Inject

class SchedulePolicyMapper @Inject constructor(
    private val actionMapper: BidirectionalMapper<ScheduleActionDo, ScheduleAction>
): BidirectionalMapper<SchedulePolicyDo, SchedulePolicy> {

    override fun map(input: SchedulePolicyDo): SchedulePolicy {
        return SchedulePolicy(
            noMatches = actionMapper.map(input.noMatches),
            badConnection = actionMapper.map(input.badConnection),
            anotherFailure = actionMapper.map(input.anotherFailure)
        )
    }

    override fun reverseMap(input: SchedulePolicy): SchedulePolicyDo {
        return SchedulePolicyDo(
            noMatches = actionMapper.reverseMap(input.noMatches),
            badConnection = actionMapper.reverseMap(input.badConnection),
            anotherFailure = actionMapper.reverseMap(input.anotherFailure)
        )
    }

}