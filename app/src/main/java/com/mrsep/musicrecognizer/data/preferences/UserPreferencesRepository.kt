package com.mrsep.musicrecognizer.data.preferences

import android.util.Log
import androidx.datastore.core.DataStore
import com.mrsep.musicrecognizer.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "UserPreferencesRepository"

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<UserPreferences>
) {

    private fun Flow<UserPreferences>.ioExceptionCatcherOnWrite(): Flow<UserPreferences> {
        return this.catch { e ->
            Log.e(TAG, "Failed to read user preferences", e)
            when (e) {
                is IOException -> emit(UserPreferences.getDefaultInstance())
                else -> throw e
            }
        }
    }

    private suspend fun ioExceptionCatcherOnRead(block: suspend () -> Unit) {
        runCatching {
            block.invoke()
        }.onFailure { e ->
            when (e) {
                is IOException -> Log.e(TAG, "Failed to update user preferences", e)
                else -> throw e
            }
        }
    }

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .ioExceptionCatcherOnWrite()


    suspend fun saveApiToken(newToken: String) {
        ioExceptionCatcherOnRead {
            dataStore.updateData { currentPreferences ->
                currentPreferences.toBuilder()
                    .setApiToken(newToken)
                    .build()
            }
        }
    }

    suspend fun setOnboardingCompleted(onboardingCompleted: Boolean) {
        ioExceptionCatcherOnRead {
            dataStore.updateData { currentPreferences ->
                currentPreferences.toBuilder()
                    .setOnboardingCompleted(onboardingCompleted)
                    .build()
            }
        }
    }

}