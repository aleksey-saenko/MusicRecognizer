package com.mrsep.musicrecognizer.glue.preferences.mapper

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo.SchedulePolicyDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo.RequiredServicesDo
import com.mrsep.musicrecognizer.feature.preferences.domain.UserPreferences
import com.mrsep.musicrecognizer.feature.preferences.domain.UserPreferences.SchedulePolicy
import com.mrsep.musicrecognizer.feature.preferences.domain.UserPreferences.RequiredServices
import javax.inject.Inject

class PreferencesMapper @Inject constructor(
    private val schedulePolicyMapper: BidirectionalMapper<SchedulePolicyDo, SchedulePolicy>,
    private val requiredServicesMapper: BidirectionalMapper<RequiredServicesDo, RequiredServices>
) :
    Mapper<UserPreferencesDo, UserPreferences> {

    override fun map(input: UserPreferencesDo): UserPreferences {
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