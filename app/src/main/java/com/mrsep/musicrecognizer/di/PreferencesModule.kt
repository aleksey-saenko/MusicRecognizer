package com.mrsep.musicrecognizer.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import androidx.datastore.dataStoreFile
import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesProtoSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val USER_PREFERENCES_STORE = "USER_PREFERENCES_STORE"

private val Context.userPreferencesDataStore: DataStore<UserPreferencesProto> by dataStore(
    fileName = USER_PREFERENCES_STORE,
    serializer = UserPreferencesProtoSerializer,
    corruptionHandler = ReplaceFileCorruptionHandler(
        produceNewData = { UserPreferencesProto.getDefaultInstance() }
    )
)

@Module
@InstallIn(SingletonComponent::class)
class PreferencesModule {

    @Provides
    @Singleton
    fun provideUserPreferences(@ApplicationContext appContext: Context): DataStore<UserPreferencesProto> {
        return DataStoreFactory.create(
            serializer = UserPreferencesProtoSerializer,
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { UserPreferencesProto.getDefaultInstance() }
            ),
            produceFile = { appContext.dataStoreFile(USER_PREFERENCES_STORE) }
        )
    }

}