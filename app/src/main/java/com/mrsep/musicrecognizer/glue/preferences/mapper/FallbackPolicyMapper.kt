package com.mrsep.musicrecognizer.glue.preferences.mapper

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.data.preferences.FallbackActionDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo.FallbackPolicyDo
import com.mrsep.musicrecognizer.feature.preferences.domain.FallbackAction
import com.mrsep.musicrecognizer.feature.preferences.domain.UserPreferences.FallbackPolicy
import javax.inject.Inject

class FallbackPolicyMapper @Inject constructor(
    private val actionMapper: BidirectionalMapper<FallbackActionDo, FallbackAction>
) : BidirectionalMapper<FallbackPolicyDo, FallbackPolicy> {

    override fun map(input: FallbackPolicyDo): FallbackPolicy {
        return FallbackPolicy(
            noMatches = actionMapper.map(input.noMatches),
            badConnection = actionMapper.map(input.badConnection),
            anotherFailure = actionMapper.map(input.anotherFailure)
        )
    }

    override fun reverseMap(input: FallbackPolicy): FallbackPolicyDo {
        return FallbackPolicyDo(
            noMatches = actionMapper.reverseMap(input.noMatches),
            badConnection = actionMapper.reverseMap(input.badConnection),
            anotherFailure = actionMapper.reverseMap(input.anotherFailure)
        )
    }
}
