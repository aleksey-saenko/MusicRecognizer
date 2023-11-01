package com.mrsep.musicrecognizer.data.preferences

import android.util.Log
import androidx.datastore.core.DataStore
import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.UserPreferencesProto.FallbackPolicyProto
import com.mrsep.musicrecognizer.UserPreferencesProto.RequiredServicesProto
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
    private val requiredServicesMapper: BidirectionalMapper<RequiredServicesProto, UserPreferencesDo.RequiredServicesDo>,
    private val fallbackPolicyMapper: BidirectionalMapper<FallbackPolicyProto, UserPreferencesDo.FallbackPolicyDo>,
    private val lyricsFontStyleMapper: BidirectionalMapper<UserPreferencesProto.LyricsFontStyleProto, UserPreferencesDo.LyricsFontStyleDo>,
    private val trackFilterMapper: BidirectionalMapper<UserPreferencesProto.TrackFilterProto, UserPreferencesDo.TrackFilterDo>,
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

    override suspend fun setRequiredServices(value: UserPreferencesDo.RequiredServicesDo) {
        safeWriter {
            setRequiredServices(requiredServicesMapper.reverseMap(value))
        }
    }

    override suspend fun setDeveloperModeEnabled(value: Boolean) {
        safeWriter { setDeveloperModeEnabled(value) }
    }

    override suspend fun setFallbackPolicy(value: UserPreferencesDo.FallbackPolicyDo) {
        safeWriter {
            setFallbackPolicy(fallbackPolicyMapper.reverseMap(value))
        }
    }

    override suspend fun setLyricsFontStyle(value: UserPreferencesDo.LyricsFontStyleDo) {
        safeWriter {
            setLyricsFontStyle(lyricsFontStyleMapper.reverseMap(value))
        }
    }

    override suspend fun setTrackFilter(value: UserPreferencesDo.TrackFilterDo) {
        safeWriter {
            setTrackFilter(trackFilterMapper.reverseMap(value))
        }
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