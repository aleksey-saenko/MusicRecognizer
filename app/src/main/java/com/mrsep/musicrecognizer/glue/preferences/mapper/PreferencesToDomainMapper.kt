package com.mrsep.musicrecognizer.glue.preferences.mapper

import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.feature.preferences.domain.UserPreferences
import javax.inject.Inject

class PreferencesToDomainMapper @Inject constructor(
    private val schedulePolicyMapper: BidirectionalMapper<UserPreferencesProto.SchedulePolicyProto, UserPreferences.SchedulePolicy>,
    private val requiredServicesMapper: BidirectionalMapper<UserPreferencesProto.RequiredServicesProto, UserPreferences.RequiredServices>
) :
    Mapper<UserPreferencesProto, UserPreferences> {

    override fun map(input: UserPreferencesProto): UserPreferences {
        return UserPreferences(
            onboardingCompleted = input.onboardingCompleted,
            apiToken = input.apiToken,
            notificationServiceEnabled = input.notificationServiceEnabled,
            dynamicColorsEnabled = input.dynamicColorsEnabled,
            developerModeEnabled = input.developerModeEnabled,
            requiredServices = requiredServicesMapper.map(input.requiredServices),
            schedulePolicy = schedulePolicyMapper.map(input.schedulePolicy)
        )
    }

}