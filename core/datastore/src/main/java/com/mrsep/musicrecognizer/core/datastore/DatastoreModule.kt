package com.mrsep.musicrecognizer.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import com.mrsep.musicrecognizer.core.common.di.ApplicationScope
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

const val USER_PREFERENCES_STORE = "USER_PREFERENCES_STORE"

@Module
@InstallIn(SingletonComponent::class)
internal object DatastoreModule {

    @Provides
    @Singleton
    fun provideUserPreferences(
        @ApplicationContext appContext: Context,
        @ApplicationScope appScope: CoroutineScope,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): DataStore<UserPreferencesProto> {
        return DataStoreFactory.create(
            serializer = UserPreferencesProtoSerializer,
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { UserPreferencesProto.getDefaultInstance() }
            ),
            migrations = listOf(
                RequiredMusicServicesMigration,
            ),
            scope = CoroutineScope(appScope.coroutineContext + ioDispatcher),
            produceFile = { appContext.dataStoreFile(USER_PREFERENCES_STORE) }
        )
    }
}
