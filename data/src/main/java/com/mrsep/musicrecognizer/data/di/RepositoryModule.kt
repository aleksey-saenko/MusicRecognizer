package com.mrsep.musicrecognizer.data.di

import com.mrsep.musicrecognizer.data.enqueued.EnqueuedRecognitionRepositoryDo
import com.mrsep.musicrecognizer.data.enqueued.EnqueuedRecognitionRepositoryImpl
import com.mrsep.musicrecognizer.data.enqueued.RecordingFileDataSource
import com.mrsep.musicrecognizer.data.enqueued.RecordingFileDataSourceImpl
import com.mrsep.musicrecognizer.data.preferences.PreferencesRepositoryDo
import com.mrsep.musicrecognizer.data.preferences.PreferencesRepositoryImpl
import com.mrsep.musicrecognizer.data.track.TrackRepositoryDo
import com.mrsep.musicrecognizer.data.track.TrackRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    @Singleton
    fun bindTrackRepository(implementation: TrackRepositoryImpl): TrackRepositoryDo

    @Binds
    @Singleton
    fun bindPreferencesRepository(implementation: PreferencesRepositoryImpl): PreferencesRepositoryDo

    @Binds
    @Singleton
    fun bindEnqueuedRecognitionRepository(implementation: EnqueuedRecognitionRepositoryImpl): EnqueuedRecognitionRepositoryDo

    @Binds
    @Singleton
    fun bindRecordingFileDataSource(implementation: RecordingFileDataSourceImpl): RecordingFileDataSource

}