package com.mrsep.musicrecognizer.glue.preferences.mapper

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.preferences.AudioCaptureModeDo
import com.mrsep.musicrecognizer.data.preferences.ThemeModeDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo.*
import com.mrsep.musicrecognizer.data.remote.AcrCloudConfigDo
import com.mrsep.musicrecognizer.data.remote.AuddConfigDo
import com.mrsep.musicrecognizer.data.remote.RecognitionProviderDo
import com.mrsep.musicrecognizer.data.track.MusicServiceDo
import com.mrsep.musicrecognizer.feature.preferences.domain.AcrCloudConfig
import com.mrsep.musicrecognizer.feature.preferences.domain.AuddConfig
import com.mrsep.musicrecognizer.feature.preferences.domain.AudioCaptureMode
import com.mrsep.musicrecognizer.feature.preferences.domain.MusicService
import com.mrsep.musicrecognizer.feature.preferences.domain.RecognitionProvider
import com.mrsep.musicrecognizer.feature.preferences.domain.ThemeMode
import com.mrsep.musicrecognizer.feature.preferences.domain.UserPreferences
import com.mrsep.musicrecognizer.feature.preferences.domain.UserPreferences.*
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

class UserPreferencesMapper @Inject constructor(
    private val fallbackPolicyMapper: BidirectionalMapper<FallbackPolicyDo, FallbackPolicy>,
    private val musicServiceMapper: BidirectionalMapper<MusicServiceDo, MusicService>,
    private val hapticFeedbackMapper: BidirectionalMapper<HapticFeedbackDo, HapticFeedback>,
    private val themeModeMapper: BidirectionalMapper<ThemeModeDo, ThemeMode>,
    private val providerMapper: BidirectionalMapper<RecognitionProviderDo, RecognitionProvider>,
    private val auddConfigMapper: BidirectionalMapper<AuddConfigDo, AuddConfig>,
    private val acrCloudConfigMapper: BidirectionalMapper<AcrCloudConfigDo, AcrCloudConfig>,
    private val audioCaptureModeMapper: BidirectionalMapper<AudioCaptureModeDo, AudioCaptureMode>,
) : Mapper<UserPreferencesDo, UserPreferences> {

    override fun map(input: UserPreferencesDo): UserPreferences {
        return UserPreferences(
            onboardingCompleted = input.onboardingCompleted,
            currentRecognitionProvider = providerMapper.map(input.currentRecognitionProvider),
            auddConfig = auddConfigMapper.map(input.auddConfig),
            acrCloudConfig = acrCloudConfigMapper.map(input.acrCloudConfig),
            defaultAudioCaptureMode = audioCaptureModeMapper.map(input.defaultAudioCaptureMode),
            mainButtonLongPressAudioCaptureMode = audioCaptureModeMapper.map(input.mainButtonLongPressAudioCaptureMode),
            fallbackPolicy = fallbackPolicyMapper.map(input.fallbackPolicy),
            recognizeOnStartup = input.recognizeOnStartup,
            notificationServiceEnabled = input.notificationServiceEnabled,
            artworkBasedThemeEnabled = input.artworkBasedThemeEnabled,
            requiredMusicServices = input.requiredMusicServices
                .map(musicServiceMapper::map)
                .toImmutableList(),
            hapticFeedback = hapticFeedbackMapper.map(input.hapticFeedback),
            themeMode = themeModeMapper.map(input.themeMode),
            usePureBlackForDarkTheme = input.usePureBlackForDarkTheme,
            dynamicColorsEnabled = input.dynamicColorsEnabled,
        )
    }
}
