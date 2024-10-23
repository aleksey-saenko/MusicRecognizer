package com.mrsep.musicrecognizer.data.preferences

import com.mrsep.musicrecognizer.data.remote.AcrCloudConfigDo
import com.mrsep.musicrecognizer.data.remote.AuddConfigDo
import com.mrsep.musicrecognizer.data.remote.RecognitionProviderDo
import com.mrsep.musicrecognizer.data.track.MusicServiceDo
import kotlinx.coroutines.flow.Flow

interface PreferencesRepositoryDo {

    val userPreferencesFlow: Flow<UserPreferencesDo>

    suspend fun setOnboardingCompleted(value: Boolean)
    suspend fun setCurrentRecognitionProvider(value: RecognitionProviderDo)
    suspend fun setAuddConfig(value: AuddConfigDo)
    suspend fun setAcrCloudConfig(value: AcrCloudConfigDo)
    suspend fun setDefaultAudioCaptureMode(value: AudioCaptureModeDo)
    suspend fun setMainButtonLongPressAudioCaptureMode(value: AudioCaptureModeDo)
    suspend fun setFallbackPolicy(value: UserPreferencesDo.FallbackPolicyDo)
    suspend fun setRecognizeOnStartup(value: Boolean)
    suspend fun setNotificationServiceEnabled(value: Boolean)
    suspend fun setDynamicColorsEnabled(value: Boolean)
    suspend fun setArtworkBasedThemeEnabled(value: Boolean)
    suspend fun setRequiredMusicServices(services: List<MusicServiceDo>)
    suspend fun setLyricsFontStyle(value: UserPreferencesDo.LyricsFontStyleDo)
    suspend fun setTrackFilter(value: UserPreferencesDo.TrackFilterDo)
    suspend fun setHapticFeedback(value: UserPreferencesDo.HapticFeedbackDo)
    suspend fun setUseGridForLibrary(value: Boolean)
    suspend fun setUseGridForRecognitionQueue(value: Boolean)
    suspend fun setShowRecognitionDateInLibrary(value: Boolean)
    suspend fun setShowCreationDateInQueue(value: Boolean)
    suspend fun setThemeMode(value: ThemeModeDo)
    suspend fun setUsePureBlackForDarkTheme(value: Boolean)
}
