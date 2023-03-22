package com.mrsep.musicrecognizer.data.preferences

import android.util.Log
import androidx.datastore.core.DataStore
import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.UserPreferencesProto.RequiredServicesProto
import com.mrsep.musicrecognizer.di.DefaultDispatcher
import com.mrsep.musicrecognizer.di.IoDispatcher
import com.mrsep.musicrecognizer.domain.PreferencesRepository
import com.mrsep.musicrecognizer.domain.model.Mapper
import com.mrsep.musicrecognizer.domain.model.UserPreferences
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PreferencesRepositoryImpl"

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<UserPreferencesProto>,
    private val preferencesToDomainMapper: Mapper<UserPreferencesProto, UserPreferences>,
    private val requiredServicesToProtoMapper: Mapper<UserPreferences.RequiredServices, RequiredServicesProto>,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher defaultDispatcher: CoroutineDispatcher
) : PreferencesRepository {

    private fun Flow<UserPreferencesProto>.ioExceptionCatcherOnRead(): Flow<UserPreferencesProto> {
        return this.catch { e ->
            Log.e(TAG, "Failed to read user preferences", e)
            when (e) {
                is IOException -> emit(UserPreferencesProto.getDefaultInstance())
                else -> throw e
            }
        }
    }

    private suspend fun safeWriter(
        action: UserPreferencesProto.Builder.() -> UserPreferencesProto.Builder
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

    override val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .ioExceptionCatcherOnRead()
        .flowOn(ioDispatcher)
        .map { preferencesProto -> preferencesToDomainMapper.map(preferencesProto) }
        .flowOn(defaultDispatcher)

    override suspend fun saveApiToken(newToken: String) {
        safeWriter { setApiToken(newToken) }
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

    override suspend fun setRequiredServices(requiredServices: UserPreferences.RequiredServices) {
        safeWriter { setRequiredServices(requiredServicesToProtoMapper.map(requiredServices)) }
    }

    override suspend fun setDeveloperModeEnabled(value: Boolean) {
        safeWriter { setDeveloperModeEnabled(value) }
    }
}