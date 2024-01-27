package com.mrsep.musicrecognizer.glue.onboarding.mapper

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.data.remote.AuddConfigDo
import com.mrsep.musicrecognizer.feature.onboarding.domain.model.AuddConfig
import javax.inject.Inject

class AuddConfigMapper @Inject constructor() : BidirectionalMapper<AuddConfigDo, AuddConfig> {

    override fun map(input: AuddConfigDo): AuddConfig {
        return AuddConfig(
            apiToken = input.apiToken
        )
    }

    override fun reverseMap(input: AuddConfig): AuddConfigDo {
        return AuddConfigDo(
            apiToken = input.apiToken
        )
    }

}