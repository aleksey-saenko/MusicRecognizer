package com.mrsep.musicrecognizer.glue.preferences.mapper

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.preferences.ThemeModeDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo.*
import com.mrsep.musicrecognizer.feature.preferences.domain.ThemeMode
import com.mrsep.musicrecognizer.feature.preferences.domain.UserPreferences
import com.mrsep.musicrecognizer.feature.preferences.domain.UserPreferences.*
import javax.inject.Inject

class PreferencesMapper @Inject constructor(
    private val fallbackPolicyMapper: BidirectionalMapper<FallbackPolicyDo, FallbackPolicy>,
    private val requiredServicesMapper: BidirectionalMapper<RequiredServicesDo, RequiredServices>,
    private val hapticFeedbackMapper: BidirectionalMapper<HapticFeedbackDo, HapticFeedback>,
    private val themeModeMapper: BidirectionalMapper<ThemeModeDo, ThemeMode>,
) :
    Mapper<UserPreferencesDo, UserPreferences> {

    override fun map(input: UserPreferencesDo): UserPreferences {
        return UserPreferences(
            onboardingCompleted = input.onboardingCompleted,
            apiToken = input.apiToken,
            notificationServiceEnabled = input.notificationServiceEnabled,
            dynamicColorsEnabled = input.dynamicColorsEnabled,
            artworkBasedThemeEnabled = input.artworkBasedThemeEnabled,
            developerModeEnabled = input.developerModeEnabled,
            requiredServices = requiredServicesMapper.map(input.requiredServices),
            fallbackPolicy = fallbackPolicyMapper.map(input.fallbackPolicy),
            hapticFeedback = hapticFeedbackMapper.map(input.hapticFeedback),
            useColumnForLibrary = input.useColumnForLibrary,
            themeMode = themeModeMapper.map(input.themeMode),
            usePureBlackForDarkTheme = input.usePureBlackForDarkTheme
        )
    }

}