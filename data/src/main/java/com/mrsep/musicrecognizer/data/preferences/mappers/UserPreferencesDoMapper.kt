package com.mrsep.musicrecognizer.data.preferences.mappers

import com.mrsep.musicrecognizer.MusicServiceProto
import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.UserPreferencesProto.*
import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.data.preferences.ThemeModeDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo.*
import com.mrsep.musicrecognizer.data.track.MusicServiceDo
import javax.inject.Inject

class UserPreferencesDoMapper @Inject constructor(
    private val fallbackPolicyMapper: BidirectionalMapper<FallbackPolicyProto, FallbackPolicyDo>,
    private val musicServiceMapper: BidirectionalMapper<MusicServiceProto?, MusicServiceDo?>,
    private val lyricsFontStyleMapper: BidirectionalMapper<LyricsFontStyleProto, LyricsFontStyleDo>,
    private val trackFilterMapper: BidirectionalMapper<TrackFilterProto, TrackFilterDo>,
    private val hapticFeedbackMapper: BidirectionalMapper<HapticFeedbackProto, HapticFeedbackDo>,
    private val themeModeMapper: BidirectionalMapper<ThemeModeProto, ThemeModeDo>,
) : BidirectionalMapper<UserPreferencesProto, UserPreferencesDo> {

    override fun reverseMap(input: UserPreferencesDo): UserPreferencesProto {
        return newBuilder()
            .setOnboardingCompleted(input.onboardingCompleted)
            .setApiToken(input.apiToken)
            .addAllRequiredMusicServices(
                input.requiredMusicServices.map(musicServiceMapper::reverseMap)
            )
            .setNotificationServiceEnabled(input.notificationServiceEnabled)
            .setDynamicColorsEnabled(input.dynamicColorsEnabled)
            .setArtworkBasedThemeEnabled(input.artworkBasedThemeEnabled)
            .setDeveloperModeEnabled(input.developerModeEnabled)
            .setFallbackPolicy(fallbackPolicyMapper.reverseMap(input.fallbackPolicy))
            .setLyricsFontStyle(lyricsFontStyleMapper.reverseMap(input.lyricsFontStyle))
            .setTrackFilter(trackFilterMapper.reverseMap(input.trackFilter))
            .setHapticFeedback(hapticFeedbackMapper.reverseMap(input.hapticFeedback))
            .setUseColumnForLibrary(input.useColumnForLibrary)
            .setThemeMode(themeModeMapper.reverseMap(input.themeMode))
            .setUsePureBlackForDarkTheme(input.usePureBlackForDarkTheme)
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
            requiredMusicServices = input.requiredMusicServicesList
                .mapNotNull { serviceProto -> musicServiceMapper.map(serviceProto) },
            fallbackPolicy = fallbackPolicyMapper.map(input.fallbackPolicy),
            lyricsFontStyle = lyricsFontStyleMapper.map(input.lyricsFontStyle),
            trackFilter = trackFilterMapper.map(input.trackFilter),
            hapticFeedback = hapticFeedbackMapper.map(input.hapticFeedback),
            useColumnForLibrary = input.useColumnForLibrary,
            themeMode = themeModeMapper.map(input.themeMode),
            usePureBlackForDarkTheme = input.usePureBlackForDarkTheme,
        )
    }

}