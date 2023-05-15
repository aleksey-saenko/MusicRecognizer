package com.mrsep.musicrecognizer.data.preferences

import android.util.Log
import androidx.datastore.core.DataStore
import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.UserPreferencesProto.SchedulePolicyProto
import com.mrsep.musicrecognizer.UserPreferencesProto.RequiredServicesProto
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

private const val TAG = "PreferencesRepositoryImpl"

class PreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<UserPreferencesProto>,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : PreferencesDataRepository {

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

    override val userPreferencesFlow: Flow<UserPreferencesProto> = dataStore.data
        .ioExceptionCatcherOnRead()
        .flowOn(ioDispatcher)

    override suspend fun saveApiToken(value: String) {
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

    override suspend fun setRequiredServices(value: RequiredServicesProto) {
        safeWriter { setRequiredServices(value) }
    }

    override suspend fun setDeveloperModeEnabled(value: Boolean) {
        safeWriter { setDeveloperModeEnabled(value) }
    }

    override suspend fun setSchedulePolicy(value: SchedulePolicyProto) {
        safeWriter { setSchedulePolicy(value) }
    }

}