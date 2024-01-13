package com.mrsep.musicrecognizer.data.preferences

import android.util.Log
import androidx.datastore.core.DataStore
import com.mrsep.musicrecognizer.MusicServiceProto
import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.UserPreferencesProto.*
import com.mrsep.musicrecognizer.UserPreferencesProtoKt
import com.mrsep.musicrecognizer.copy
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo.*
import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
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

class PreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<UserPreferencesProto>,
    private val preferencesMapper: Mapper<UserPreferencesProto, UserPreferencesDo>,
    private val musicServiceMapper: BidirectionalMapper<MusicServiceProto?, MusicServiceDo?>,
    private val fallbackPolicyMapper: BidirectionalMapper<FallbackPolicyProto, FallbackPolicyDo>,
    private val lyricsFontStyleMapper: BidirectionalMapper<LyricsFontStyleProto, LyricsFontStyleDo>,
    private val trackFilterMapper: BidirectionalMapper<TrackFilterProto, TrackFilterDo>,
    private val hapticFeedbackMapper: BidirectionalMapper<HapticFeedbackProto, HapticFeedbackDo>,
    private val themeModeMapper: BidirectionalMapper<ThemeModeProto, ThemeModeDo>,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : PreferencesRepositoryDo {

    override val userPreferencesFlow: Flow<UserPreferencesDo> = dataStore.data
        .ioExceptionCatcherOnRead()
        .map(preferencesMapper::map)
        .flowOn(ioDispatcher)

    override suspend fun setApiToken(value: String) {
        safeWriter { apiToken = value }
    }

    override suspend fun setOnboardingCompleted(value: Boolean) {
        safeWriter { onboardingCompleted = value }
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

    override suspend fun setDeveloperModeEnabled(value: Boolean) {
        safeWriter { developerModeEnabled = value }
    }

    override suspend fun setFallbackPolicy(value: FallbackPolicyDo) {
        safeWriter { fallbackPolicy = fallbackPolicyMapper.reverseMap(value) }
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

    override suspend fun setUseColumnForLibrary(value: Boolean) {
        safeWriter { useColumnForLibrary = value }
    }

    override suspend fun setThemeMode(value: ThemeModeDo) {
        safeWriter { themeMode = themeModeMapper.reverseMap(value) }
    }

    override suspend fun setUsePureBlackForDarkTheme(value: Boolean) {
        safeWriter { usePureBlackForDarkTheme = value }
    }

    override suspend fun setRecognizeOnStartup(value: Boolean) {
        safeWriter { recognizeOnStartup = value }
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