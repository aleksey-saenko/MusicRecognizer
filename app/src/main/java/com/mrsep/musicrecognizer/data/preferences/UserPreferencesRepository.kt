package com.mrsep.musicrecognizer.data.preferences

import androidx.datastore.core.DataStore
import com.mrsep.musicrecognizer.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<UserPreferences>
) {

    private fun Flow<UserPreferences>.defaultIoExceptionCatcher(): Flow<UserPreferences> {
        return this.catch {
            it.printStackTrace()
            when (it) {
                is IOException -> emit(UserPreferences.getDefaultInstance())
                else -> throw it
            }
        }
    }

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .defaultIoExceptionCatcher()



    suspend fun saveApiToken(newToken: String) {
        dataStore.updateData { currentPreferences ->
            currentPreferences.toBuilder()
                .setApiToken(newToken)
                .build()
        }
    }

    suspend fun setOnboardingCompleted(onboardingCompleted: Boolean) {
        dataStore.updateData { currentPreferences ->
            currentPreferences.toBuilder()
                .setOnboardingCompleted(onboardingCompleted)
                .build()
        }
    }

}