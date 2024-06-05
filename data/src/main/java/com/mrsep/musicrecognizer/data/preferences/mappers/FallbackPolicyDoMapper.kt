package com.mrsep.musicrecognizer.data.preferences.mappers

import com.mrsep.musicrecognizer.UserPreferencesProto.FallbackActionProto
import com.mrsep.musicrecognizer.UserPreferencesProto.FallbackPolicyProto
import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.data.preferences.FallbackActionDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo.*
import javax.inject.Inject

class FallbackPolicyDoMapper @Inject constructor(
    private val actionMapper: BidirectionalMapper<FallbackActionProto, FallbackActionDo>
) : BidirectionalMapper<FallbackPolicyProto, FallbackPolicyDo> {

    override fun map(input: FallbackPolicyProto): FallbackPolicyDo {
        return FallbackPolicyDo(
            noMatches = actionMapper.map(input.noMatches),
            badConnection = actionMapper.map(input.badConnection),
            anotherFailure = actionMapper.map(input.anotherFailure)
        )
    }

    override fun reverseMap(input: FallbackPolicyDo): FallbackPolicyProto {
        return FallbackPolicyProto.newBuilder()
            .setNoMatches(actionMapper.reverseMap(input.noMatches))
            .setBadConnection(actionMapper.reverseMap(input.badConnection))
            .setAnotherFailure(actionMapper.reverseMap(input.anotherFailure))
            .build()
    }
}
