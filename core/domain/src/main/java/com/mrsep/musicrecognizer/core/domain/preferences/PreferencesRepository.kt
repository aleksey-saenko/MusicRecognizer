package com.mrsep.musicrecognizer.core.domain.preferences

import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionProvider
import com.mrsep.musicrecognizer.core.domain.track.model.MusicService
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {

    val userPreferencesFlow: Flow<UserPreferences>

    suspend fun setOnboardingCompleted(value: Boolean)
    suspend fun setCurrentRecognitionProvider(value: RecognitionProvider)
    suspend fun setAuddConfig(value: AuddConfig)
    suspend fun setAcrCloudConfig(value: AcrCloudConfig)
    suspend fun setDefaultAudioCaptureMode(value: AudioCaptureMode)
    suspend fun setMainButtonLongPressAudioCaptureMode(value: AudioCaptureMode)
    suspend fun setFallbackPolicy(value: FallbackPolicy)
    suspend fun setRecognizeOnStartup(value: Boolean)
    suspend fun setNotificationServiceEnabled(value: Boolean)
    suspend fun setDynamicColorsEnabled(value: Boolean)
    suspend fun setArtworkBasedThemeEnabled(value: Boolean)
    suspend fun setRequiredMusicServices(services: List<MusicService>)
    suspend fun setLyricsFontStyle(value: LyricsFontStyle)
    suspend fun setTrackFilter(value: TrackFilter)
    suspend fun setHapticFeedback(value: HapticFeedback)
    suspend fun setUseGridForLibrary(value: Boolean)
    suspend fun setUseGridForRecognitionQueue(value: Boolean)
    suspend fun setShowRecognitionDateInLibrary(value: Boolean)
    suspend fun setShowCreationDateInQueue(value: Boolean)
    suspend fun setThemeMode(value: ThemeMode)
    suspend fun setUsePureBlackForDarkTheme(value: Boolean)
}
