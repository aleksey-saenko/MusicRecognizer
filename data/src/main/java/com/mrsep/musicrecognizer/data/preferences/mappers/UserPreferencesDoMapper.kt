package com.mrsep.musicrecognizer.data.preferences.mappers

import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.UserPreferencesProto.*
import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo.*
import javax.inject.Inject

class UserPreferencesDoMapper @Inject constructor(
    private val fallbackPolicyMapper: BidirectionalMapper<FallbackPolicyProto, FallbackPolicyDo>,
    private val requiredServicesMapper: BidirectionalMapper<RequiredServicesProto, RequiredServicesDo>,
    private val lyricsFontStyleMapper: BidirectionalMapper<LyricsFontStyleProto, LyricsFontStyleDo>,
    private val trackFilterMapper: BidirectionalMapper<TrackFilterProto, TrackFilterDo>,
    private val hapticFeedbackMapper: BidirectionalMapper<HapticFeedbackProto, HapticFeedbackDo>,
) : BidirectionalMapper<UserPreferencesProto, UserPreferencesDo> {

    override fun reverseMap(input: UserPreferencesDo): UserPreferencesProto {
        return newBuilder()
            .setOnboardingCompleted(input.onboardingCompleted)
            .setApiToken(input.apiToken)
            .setRequiredServices(requiredServicesMapper.reverseMap(input.requiredServices))
            .setNotificationServiceEnabled(input.notificationServiceEnabled)
            .setDynamicColorsEnabled(input.dynamicColorsEnabled)
            .setArtworkBasedThemeEnabled(input.artworkBasedThemeEnabled)
            .setDeveloperModeEnabled(input.developerModeEnabled)
            .setFallbackPolicy(fallbackPolicyMapper.reverseMap(input.fallbackPolicy))
            .setLyricsFontStyle(lyricsFontStyleMapper.reverseMap(input.lyricsFontStyle))
            .setTrackFilter(trackFilterMapper.reverseMap(input.trackFilter))
            .setHapticFeedback(hapticFeedbackMapper.reverseMap(input.hapticFeedback))
            .setUseGridForLibrary(input.useGridForLibrary)
            .build()
    }

    override fun map(input: UserPreferencesProto): UserPreferencesDo {
        return UserPreferencesDo(
            onboardingCompleted = input.onboardingCompleted,
            apiToken = input.apiToken,
            notificationServiceEnabled = input.notificationServiceEnabled,
            dynamicColorsEnabled = input.dynamicColorsEnabled,
            artworkBasedThemeEnabled = input.artworkBasedThemeEnabled,
            developerModeEnabled = input.developerModeEnabled,
            requiredServices = requiredServicesMapper.map(input.requiredServices),
            fallbackPolicy = fallbackPolicyMapper.map(input.fallbackPolicy),
            lyricsFontStyle = lyricsFontStyleMapper.map(input.lyricsFontStyle),
            trackFilter = trackFilterMapper.map(input.trackFilter),
            hapticFeedback = hapticFeedbackMapper.map(input.hapticFeedback),
            useGridForLibrary = input.useGridForLibrary
        )
    }

}