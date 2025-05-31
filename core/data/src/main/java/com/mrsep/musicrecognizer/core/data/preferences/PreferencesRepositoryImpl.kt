package com.mrsep.musicrecognizer.core.data.preferences

import android.util.Log
import androidx.datastore.core.DataStore
import com.mrsep.musicrecognizer.core.datastore.UserPreferencesProto
import com.mrsep.musicrecognizer.core.datastore.UserPreferencesProto.getDefaultInstance
import com.mrsep.musicrecognizer.core.datastore.UserPreferencesProtoKt
import com.mrsep.musicrecognizer.core.datastore.copy
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.core.domain.preferences.AcrCloudConfig
import com.mrsep.musicrecognizer.core.domain.preferences.AuddConfig
import com.mrsep.musicrecognizer.core.domain.preferences.AudioCaptureMode
import com.mrsep.musicrecognizer.core.domain.preferences.FallbackPolicy
import com.mrsep.musicrecognizer.core.domain.preferences.HapticFeedback
import com.mrsep.musicrecognizer.core.domain.preferences.LyricsStyle
import com.mrsep.musicrecognizer.core.domain.preferences.PreferencesRepository
import com.mrsep.musicrecognizer.core.domain.preferences.ThemeMode
import com.mrsep.musicrecognizer.core.domain.preferences.TrackFilter
import com.mrsep.musicrecognizer.core.domain.preferences.UserPreferences
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionProvider
import com.mrsep.musicrecognizer.core.domain.track.model.MusicService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

private const val TAG = "PreferencesRepositoryImpl"

internal class PreferencesRepositoryImpl @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val dataStore: DataStore<UserPreferencesProto>,
) : PreferencesRepository {

    override val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .ioExceptionCatcherOnRead()
        .map(UserPreferencesProto::toDomain)
        .flowOn(ioDispatcher)

    override suspend fun setOnboardingCompleted(value: Boolean) {
        safeWriter { onboardingCompleted = value }
    }

    override suspend fun setCurrentRecognitionProvider(value: RecognitionProvider) {
        safeWriter { currentRecognitionProvider = value.toProto() }
    }

    override suspend fun setAuddConfig(value: AuddConfig) {
        safeWriter { apiToken = value.apiToken }
    }

    override suspend fun setAcrCloudConfig(value: AcrCloudConfig) {
        safeWriter { acrCloudConfig = value.toProto() }
    }

    override suspend fun setDefaultAudioCaptureMode(value: AudioCaptureMode) {
        safeWriter { defaultAudioCaptureMode = value.toProto() }
    }

    override suspend fun setMainButtonLongPressAudioCaptureMode(value: AudioCaptureMode) {
        safeWriter { mainButtonLongPressAudioCaptureMode = value.toProto() }
    }

    override suspend fun setUseAltDeviceSoundSource(value: Boolean) {
        safeWriter { useAltDeviceSoundSource = value }
    }

    override suspend fun setFallbackPolicy(value: FallbackPolicy) {
        safeWriter { fallbackPolicy = value.toProto() }
    }

    override suspend fun setRecognizeOnStartup(value: Boolean) {
        safeWriter { recognizeOnStartup = value }
    }

    override suspend fun setNotificationServiceEnabled(value: Boolean) {
        safeWriter { notificationServiceEnabled = value }
    }

    override suspend fun setDynamicColorsEnabled(value: Boolean) {
        safeWriter { dynamicColorsEnabled = value }
    }

    override suspend fun setArtworkBasedThemeEnabled(value: Boolean) {
        safeWriter { artworkBasedThemeEnabled = value }
    }

    override suspend fun setRequiredMusicServices(services: List<MusicService>) {
        check(services.toSet().size == services.size) {
            "RequiredMusicServices must contain only unique values"
        }
        safeWriter {
            requiredMusicServices.clear()
            requiredMusicServices.addAll(services.map(MusicService::toProto))
        }
    }

    override suspend fun setLyricsStyle(value: LyricsStyle) {
        safeWriter { lyricsStyle = value.toProto() }
    }

    override suspend fun setTrackFilter(value: TrackFilter) {
        safeWriter { trackFilter = value.toProto() }
    }

    override suspend fun setHapticFeedback(value: HapticFeedback) {
        safeWriter { hapticFeedback = value.toProto() }
    }

    override suspend fun setUseGridForLibrary(value: Boolean) {
        safeWriter { useGridForLibrary = value }
    }

    override suspend fun setUseGridForRecognitionQueue(value: Boolean) {
        safeWriter { useGridForRecognitionQueue = value }
    }

    override suspend fun setShowRecognitionDateInLibrary(value: Boolean) {
        safeWriter { showRecognitionDateInLibrary = value }
    }

    override suspend fun setShowCreationDateInQueue(value: Boolean) {
        safeWriter { showCreationDateInQueue = value }
    }

    override suspend fun setThemeMode(value: ThemeMode) {
        safeWriter { themeMode = value.toProto() }
    }

    override suspend fun setUsePureBlackForDarkTheme(value: Boolean) {
        safeWriter { usePureBlackForDarkTheme = value }
    }

    private fun Flow<UserPreferencesProto>.ioExceptionCatcherOnRead(): Flow<UserPreferencesProto> {
        return this.catch { e ->
            Log.e(TAG, "Failed to read user preferences", e)
            when (e) {
                is IOException -> emit(getDefaultInstance())
                else -> throw e
            }
        }
    }

    private suspend inline fun safeWriter(
        crossinline action: UserPreferencesProtoKt.Dsl.() -> Unit
    ) {
        withContext(ioDispatcher) {
            try {
                dataStore.updateData { currentPreferences ->
                    currentPreferences.copy {
                        action()
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Failed to update user preferences", e)
            }
        }
    }
}
