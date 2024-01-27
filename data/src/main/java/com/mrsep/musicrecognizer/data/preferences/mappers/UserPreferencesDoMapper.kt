package com.mrsep.musicrecognizer.data.preferences.mappers

import com.mrsep.musicrecognizer.AcrCloudConfigProto
import com.mrsep.musicrecognizer.MusicServiceProto
import com.mrsep.musicrecognizer.RecognitionProviderProto
import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.UserPreferencesProto.*
import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.preferences.ThemeModeDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo.*
import com.mrsep.musicrecognizer.data.remote.AcrCloudConfigDo
import com.mrsep.musicrecognizer.data.remote.AuddConfigDo
import com.mrsep.musicrecognizer.data.remote.RecognitionProviderDo
import com.mrsep.musicrecognizer.data.track.MusicServiceDo
import javax.inject.Inject

class UserPreferencesDoMapper @Inject constructor(
    private val fallbackPolicyMapper: BidirectionalMapper<FallbackPolicyProto, FallbackPolicyDo>,
    private val musicServiceMapper: BidirectionalMapper<MusicServiceProto?, MusicServiceDo?>,
    private val lyricsFontStyleMapper: BidirectionalMapper<LyricsFontStyleProto, LyricsFontStyleDo>,
    private val trackFilterMapper: BidirectionalMapper<TrackFilterProto, TrackFilterDo>,
    private val hapticFeedbackMapper: BidirectionalMapper<HapticFeedbackProto, HapticFeedbackDo>,
    private val themeModeMapper: BidirectionalMapper<ThemeModeProto, ThemeModeDo>,
    private val acrCloudConfigMapper: BidirectionalMapper<AcrCloudConfigProto, AcrCloudConfigDo>,
    private val recognitionProviderMapper: BidirectionalMapper<RecognitionProviderProto, RecognitionProviderDo>,
) : Mapper<UserPreferencesProto, UserPreferencesDo> {

    override fun map(input: UserPreferencesProto): UserPreferencesDo {
        return UserPreferencesDo(
            onboardingCompleted = input.onboardingCompleted,
            currentRecognitionProvider = recognitionProviderMapper
                .map(input.currentRecognitionProvider),
            auddConfig = AuddConfigDo(input.apiToken),
            acrCloudConfig = acrCloudConfigMapper.map(input.acrCloudConfig),
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
            recognizeOnStartup = input.recognizeOnStartup,
        )
    }

}