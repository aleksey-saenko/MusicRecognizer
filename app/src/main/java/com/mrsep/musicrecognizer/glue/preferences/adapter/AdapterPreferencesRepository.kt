package com.mrsep.musicrecognizer.glue.preferences.adapter

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.preferences.PreferencesRepositoryDo
import com.mrsep.musicrecognizer.data.preferences.ThemeModeDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo.*
import com.mrsep.musicrecognizer.data.remote.AcrCloudConfigDo
import com.mrsep.musicrecognizer.data.remote.AuddConfigDo
import com.mrsep.musicrecognizer.data.remote.RecognitionProviderDo
import com.mrsep.musicrecognizer.data.track.MusicServiceDo
import com.mrsep.musicrecognizer.feature.preferences.domain.AcrCloudConfig
import com.mrsep.musicrecognizer.feature.preferences.domain.AuddConfig
import com.mrsep.musicrecognizer.feature.preferences.domain.MusicService
import com.mrsep.musicrecognizer.feature.preferences.domain.PreferencesRepository
import com.mrsep.musicrecognizer.feature.preferences.domain.RecognitionProvider
import com.mrsep.musicrecognizer.feature.preferences.domain.ThemeMode
import com.mrsep.musicrecognizer.feature.preferences.domain.UserPreferences
import com.mrsep.musicrecognizer.feature.preferences.domain.UserPreferences.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AdapterPreferencesRepository @Inject constructor(
    private val preferencesRepositoryDo: PreferencesRepositoryDo,
    private val preferencesMapper: Mapper<UserPreferencesDo, UserPreferences>,
    private val musicServiceMapper: BidirectionalMapper<MusicServiceDo, MusicService>,
    private val fallbackPolicyMapper: BidirectionalMapper<FallbackPolicyDo, FallbackPolicy>,
    private val hapticFeedbackMapper: BidirectionalMapper<HapticFeedbackDo, HapticFeedback>,
    private val themeModeMapper: BidirectionalMapper<ThemeModeDo, ThemeMode>,
    private val providerMapper: BidirectionalMapper<RecognitionProviderDo, RecognitionProvider>,
    private val auddConfigMapper: BidirectionalMapper<AuddConfigDo, AuddConfig>,
    private val acrCloudConfigMapper: BidirectionalMapper<AcrCloudConfigDo, AcrCloudConfig>,
) : PreferencesRepository {

    override val userPreferencesFlow: Flow<UserPreferences>
        get() = preferencesRepositoryDo.userPreferencesFlow
            .map { prefData -> preferencesMapper.map(prefData) }

    override suspend fun setCurrentRecognitionProvider(value: RecognitionProvider) {
        preferencesRepositoryDo.setCurrentRecognitionProvider(providerMapper.reverseMap(value))
    }

    override suspend fun setAuddConfig(value: AuddConfig) {
        preferencesRepositoryDo.setAuddConfig(auddConfigMapper.reverseMap(value))
    }

    override suspend fun setAcrCloudConfig(value: AcrCloudConfig) {
        preferencesRepositoryDo.setAcrCloudConfig(acrCloudConfigMapper.reverseMap(value))
    }

    override suspend fun setOnboardingCompleted(value: Boolean) {
        preferencesRepositoryDo.setOnboardingCompleted(value)
    }

    override suspend fun setNotificationServiceEnabled(value: Boolean) {
        preferencesRepositoryDo.setNotificationServiceEnabled(value)
    }

    override suspend fun setDynamicColorsEnabled(value: Boolean) {
        preferencesRepositoryDo.setDynamicColorsEnabled(value)
    }

    override suspend fun setArtworkBasedThemeEnabled(value: Boolean) {
        preferencesRepositoryDo.setArtworkBasedThemeEnabled(value)
    }

    override suspend fun setDeveloperModeEnabled(value: Boolean) {
        preferencesRepositoryDo.setDeveloperModeEnabled(value)
    }

    override suspend fun setRequiredMusicServices(services: List<MusicService>) {
        preferencesRepositoryDo.setRequiredMusicServices(
            services.map(musicServiceMapper::reverseMap)
        )
    }

    override suspend fun setFallbackPolicy(fallbackPolicy: FallbackPolicy) {
        preferencesRepositoryDo.setFallbackPolicy(
            fallbackPolicyMapper.reverseMap(fallbackPolicy)
        )
    }

    override suspend fun setHapticFeedback(hapticFeedback: HapticFeedback) {
        preferencesRepositoryDo.setHapticFeedback(
            hapticFeedbackMapper.reverseMap(hapticFeedback)
        )
    }

    override suspend fun setUseColumnForLibrary(value: Boolean) {
        preferencesRepositoryDo.setUseColumnForLibrary(value)
    }

    override suspend fun setThemeMode(value: ThemeMode) {
        preferencesRepositoryDo.setThemeMode(themeModeMapper.reverseMap(value))
    }

    override suspend fun setUsePureBlackForDarkTheme(value: Boolean) {
        preferencesRepositoryDo.setUsePureBlackForDarkTheme(value)
    }

    override suspend fun setRecognizeOnStartup(value: Boolean) {
        preferencesRepositoryDo.setRecognizeOnStartup(value)
    }

}