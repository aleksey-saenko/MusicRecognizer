package com.mrsep.musicrecognizer.data.preferences.mappers

import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.UserPreferencesProto.*
import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo.*
import javax.inject.Inject

class UserPreferencesDoMapper @Inject constructor(
    private val schedulePolicyMapper: BidirectionalMapper<SchedulePolicyProto, SchedulePolicyDo>,
    private val requiredServicesMapper: BidirectionalMapper<RequiredServicesProto, RequiredServicesDo>
) : BidirectionalMapper<UserPreferencesProto, UserPreferencesDo> {

    override fun reverseMap(input: UserPreferencesDo): UserPreferencesProto {
        return newBuilder()
            .setOnboardingCompleted(input.onboardingCompleted)
            .setApiToken(input.apiToken)
            .setRequiredServices(requiredServicesMapper.reverseMap(input.requiredServices))
            .setNotificationServiceEnabled(input.notificationServiceEnabled)
            .setDynamicColorsEnabled(input.dynamicColorsEnabled)
            .setDeveloperModeEnabled(input.developerModeEnabled)
            .setSchedulePolicy(schedulePolicyMapper.reverseMap(input.schedulePolicy))
            .build()
    }

    override fun map(input: UserPreferencesProto): UserPreferencesDo {
        return UserPreferencesDo(
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