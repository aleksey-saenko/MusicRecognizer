package com.mrsep.musicrecognizer.glue.onboarding.mapper

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import com.mrsep.musicrecognizer.data.remote.AuddConfigDo
import com.mrsep.musicrecognizer.feature.onboarding.domain.model.AuddConfig
import com.mrsep.musicrecognizer.feature.onboarding.domain.model.UserPreferences
import javax.inject.Inject

class PreferencesMapper @Inject constructor(
    private val auddConfigMapper: BidirectionalMapper<AuddConfigDo, AuddConfig>
) : Mapper<UserPreferencesDo, UserPreferences> {

    override fun map(input: UserPreferencesDo): UserPreferences {
        return UserPreferences(
            onboardingCompleted = input.onboardingCompleted,
            auddConfig = auddConfigMapper.map(input.auddConfig)
        )
    }
}
