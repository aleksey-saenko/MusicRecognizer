package com.mrsep.musicrecognizer.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import androidx.datastore.dataStoreFile
import com.mrsep.musicrecognizer.UserPreferences
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val USER_PREFERENCES_STORE = "USER_PREFERENCES_STORE"

private val Context.userPreferencesDataStore: DataStore<UserPreferences> by dataStore(
    fileName = USER_PREFERENCES_STORE,
    serializer = UserPreferencesSerializer,
    corruptionHandler = ReplaceFileCorruptionHandler(
        produceNewData = { UserPreferences.getDefaultInstance() }
    )
)

@Module
@InstallIn(SingletonComponent::class)
class PreferencesModule {

    @Provides
    @Singleton
    fun provideUserPreferences(@ApplicationContext appContext: Context): DataStore<UserPreferences> {
        return DataStoreFactory.create(
            serializer = UserPreferencesSerializer,
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { UserPreferences.getDefaultInstance() }
            ),
            produceFile = { appContext.dataStoreFile(USER_PREFERENCES_STORE) }
        )
    }

}