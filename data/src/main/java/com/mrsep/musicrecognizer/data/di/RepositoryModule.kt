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
internal interface RepositoryModule {

    @Binds
    @Singleton
    fun bindTrackRepository(impl: TrackRepositoryImpl): TrackRepositoryDo

    @Binds
    @Singleton
    fun bindPreferencesRepository(impl: PreferencesRepositoryImpl): PreferencesRepositoryDo

    @Binds
    @Singleton
    fun bindEnqRepository(impl: EnqueuedRecognitionRepositoryImpl): EnqueuedRecognitionRepositoryDo

    @Binds
    @Singleton
    fun bindRecordingFileDataSource(impl: RecordingFileDataSourceImpl): RecordingFileDataSource
}
