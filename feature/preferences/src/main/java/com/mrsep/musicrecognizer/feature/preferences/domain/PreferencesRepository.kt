package com.mrsep.musicrecognizer.feature.preferences.domain

import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {

    val userPreferencesFlow: Flow<UserPreferences>

    suspend fun setCurrentRecognitionProvider(value: RecognitionProvider)
    suspend fun setAuddConfig(value: AuddConfig)
    suspend fun setAcrCloudConfig(value: AcrCloudConfig)
    suspend fun setDefaultAudioCaptureMode(value: AudioCaptureMode)
    suspend fun setMainButtonLongPressAudioCaptureMode(value: AudioCaptureMode)
    suspend fun setOnboardingCompleted(value: Boolean)
    suspend fun setNotificationServiceEnabled(value: Boolean)
    suspend fun setDynamicColorsEnabled(value: Boolean)
    suspend fun setArtworkBasedThemeEnabled(value: Boolean)
    suspend fun setRequiredMusicServices(services: List<MusicService>)
    suspend fun setFallbackPolicy(fallbackPolicy: UserPreferences.FallbackPolicy)
    suspend fun setHapticFeedback(hapticFeedback: UserPreferences.HapticFeedback)
    suspend fun setThemeMode(value: ThemeMode)
    suspend fun setUsePureBlackForDarkTheme(value: Boolean)
    suspend fun setRecognizeOnStartup(value: Boolean)
}
