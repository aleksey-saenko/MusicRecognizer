package com.mrsep.musicrecognizer.data.preferences

import android.util.Log
import androidx.datastore.core.DataStore
import com.mrsep.musicrecognizer.AcrCloudConfigProto
import com.mrsep.musicrecognizer.AudioCaptureModeProto
import com.mrsep.musicrecognizer.MusicServiceProto
import com.mrsep.musicrecognizer.RecognitionProviderProto
import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.UserPreferencesProto.*
import com.mrsep.musicrecognizer.UserPreferencesProtoKt
import com.mrsep.musicrecognizer.copy
import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo.*
import com.mrsep.musicrecognizer.data.remote.AcrCloudConfigDo
import com.mrsep.musicrecognizer.data.remote.AuddConfigDo
import com.mrsep.musicrecognizer.data.remote.RecognitionProviderDo
import com.mrsep.musicrecognizer.data.track.MusicServiceDo
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
    private val preferencesMapper: Mapper<UserPreferencesProto, UserPreferencesDo>,
    private val musicServiceMapper: BidirectionalMapper<MusicServiceProto?, MusicServiceDo?>,
    private val fallbackPolicyMapper: BidirectionalMapper<FallbackPolicyProto, FallbackPolicyDo>,
    private val lyricsFontStyleMapper: BidirectionalMapper<LyricsFontStyleProto, LyricsFontStyleDo>,
    private val trackFilterMapper: BidirectionalMapper<TrackFilterProto, TrackFilterDo>,
    private val hapticFeedbackMapper: BidirectionalMapper<HapticFeedbackProto, HapticFeedbackDo>,
    private val themeModeMapper: BidirectionalMapper<ThemeModeProto, ThemeModeDo>,
    private val acrCloudConfigMapper: BidirectionalMapper<AcrCloudConfigProto, AcrCloudConfigDo>,
    private val recognitionProviderMapper: BidirectionalMapper<RecognitionProviderProto, RecognitionProviderDo>,
    private val audioCaptureModeMapper: BidirectionalMapper<AudioCaptureModeProto, AudioCaptureModeDo>,
) : PreferencesRepositoryDo {

    override val userPreferencesFlow: Flow<UserPreferencesDo> = dataStore.data
        .ioExceptionCatcherOnRead()
        .map(preferencesMapper::map)
        .flowOn(ioDispatcher)

    override suspend fun setOnboardingCompleted(value: Boolean) {
        safeWriter { onboardingCompleted = value }
    }

    override suspend fun setCurrentRecognitionProvider(value: RecognitionProviderDo) {
        safeWriter { currentRecognitionProvider = recognitionProviderMapper.reverseMap(value) }
    }

    override suspend fun setAuddConfig(value: AuddConfigDo) {
        safeWriter { apiToken = value.apiToken }
    }

    override suspend fun setAcrCloudConfig(value: AcrCloudConfigDo) {
        safeWriter { acrCloudConfig = acrCloudConfigMapper.reverseMap(value) }
    }

    override suspend fun setDefaultAudioCaptureMode(value: AudioCaptureModeDo) {
        safeWriter { defaultAudioCaptureMode = audioCaptureModeMapper.reverseMap(value) }
    }

    override suspend fun setMainButtonLongPressAudioCaptureMode(value: AudioCaptureModeDo) {
        safeWriter { mainButtonLongPressAudioCaptureMode = audioCaptureModeMapper.reverseMap(value) }
    }

    override suspend fun setFallbackPolicy(value: FallbackPolicyDo) {
        safeWriter { fallbackPolicy = fallbackPolicyMapper.reverseMap(value) }
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

    override suspend fun setRequiredMusicServices(services: List<MusicServiceDo>) {
        check(services.toSet().size == services.size) {
            "RequiredMusicServices must contain only unique values"
        }
        safeWriter {
            requiredMusicServices.clear()
            requiredMusicServices.addAll(
                services.mapNotNull { serviceDo -> musicServiceMapper.reverseMap(serviceDo) }
            )
        }
    }

    override suspend fun setLyricsFontStyle(value: LyricsFontStyleDo) {
        safeWriter { lyricsFontStyle = lyricsFontStyleMapper.reverseMap(value) }
    }

    override suspend fun setTrackFilter(value: TrackFilterDo) {
        safeWriter { trackFilter = trackFilterMapper.reverseMap(value) }
    }

    override suspend fun setHapticFeedback(value: HapticFeedbackDo) {
        safeWriter { hapticFeedback = hapticFeedbackMapper.reverseMap(value) }
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

    override suspend fun setThemeMode(value: ThemeModeDo) {
        safeWriter { themeMode = themeModeMapper.reverseMap(value) }
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
