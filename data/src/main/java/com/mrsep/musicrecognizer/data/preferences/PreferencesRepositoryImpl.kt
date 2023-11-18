package com.mrsep.musicrecognizer.data.preferences

import android.util.Log
import androidx.datastore.core.DataStore
import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.UserPreferencesProto.*
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo.*
import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
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
    private val preferencesMapper: BidirectionalMapper<UserPreferencesProto, UserPreferencesDo>,
    private val requiredServicesMapper: BidirectionalMapper<RequiredServicesProto, RequiredServicesDo>,
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
        safeWriter { setApiToken(value) }
    }

    override suspend fun setOnboardingCompleted(value: Boolean) {
        safeWriter { setOnboardingCompleted(value) }
    }

    override suspend fun setNotificationServiceEnabled(value: Boolean) {
        safeWriter { setNotificationServiceEnabled(value) }
    }


    override suspend fun setDynamicColorsEnabled(value: Boolean) {
        safeWriter { setDynamicColorsEnabled(value) }
    }

    override suspend fun setArtworkBasedThemeEnabled(value: Boolean) {
        safeWriter { setArtworkBasedThemeEnabled(value) }
    }

    override suspend fun setRequiredServices(value: RequiredServicesDo) {
        safeWriter {
            setRequiredServices(requiredServicesMapper.reverseMap(value))
        }
    }

    override suspend fun setDeveloperModeEnabled(value: Boolean) {
        safeWriter { setDeveloperModeEnabled(value) }
    }

    override suspend fun setFallbackPolicy(value: FallbackPolicyDo) {
        safeWriter {
            setFallbackPolicy(fallbackPolicyMapper.reverseMap(value))
        }
    }

    override suspend fun setLyricsFontStyle(value: LyricsFontStyleDo) {
        safeWriter {
            setLyricsFontStyle(lyricsFontStyleMapper.reverseMap(value))
        }
    }

    override suspend fun setTrackFilter(value: TrackFilterDo) {
        safeWriter {
            setTrackFilter(trackFilterMapper.reverseMap(value))
        }
    }

    override suspend fun setHapticFeedback(value: HapticFeedbackDo) {
        safeWriter {
            setHapticFeedback(hapticFeedbackMapper.reverseMap(value))
        }
    }

    override suspend fun setUseColumnForLibrary(value: Boolean) {
        safeWriter { setUseColumnForLibrary(value) }
    }

    override suspend fun setThemeMode(value: ThemeModeDo) {
        safeWriter { setThemeMode(themeModeMapper.reverseMap(value)) }
    }

    override suspend fun setUsePureBlackForDarkTheme(value: Boolean) {
        safeWriter { setUsePureBlackForDarkTheme(value) }
    }

    private fun Flow<UserPreferencesProto>.ioExceptionCatcherOnRead(): Flow<UserPreferencesProto> {
        return this.catch { e ->
            Log.e(TAG, "Failed to read user preferences", e)
            when (e) {
                is IOException -> emit(UserPreferencesProto.getDefaultInstance())
                else -> throw e
            }
        }
    }

    private suspend inline fun safeWriter(
        crossinline action: UserPreferencesProto.Builder.() -> UserPreferencesProto.Builder
    ) {
        withContext(ioDispatcher) {
            try {
                dataStore.updateData { currentPreferences ->
                    currentPreferences.toBuilder()
                        .action()
                        .build()
                }
            } catch (e: IOException) {
                Log.e(TAG, "Failed to update user preferences", e)
            }
        }
    }

}