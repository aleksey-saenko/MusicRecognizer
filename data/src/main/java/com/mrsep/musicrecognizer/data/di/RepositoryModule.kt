package com.mrsep.musicrecognizer.data.di

import com.mrsep.musicrecognizer.data.enqueued.EnqueuedRecognitionDataRepository
import com.mrsep.musicrecognizer.data.enqueued.EnqueuedRecognitionRepositoryImpl
import com.mrsep.musicrecognizer.data.preferences.PreferencesDataRepository
import com.mrsep.musicrecognizer.data.preferences.PreferencesRepositoryImpl
import com.mrsep.musicrecognizer.data.recorder.FileRecordDataRepository
import com.mrsep.musicrecognizer.data.recorder.FileRecordRepositoryImpl
import com.mrsep.musicrecognizer.data.track.TrackDataRepository
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
    fun bindTrackRepository(implementation: TrackRepositoryImpl): TrackDataRepository

    @Binds
    @Singleton
    fun bindPreferencesRepository(implementation: PreferencesRepositoryImpl): PreferencesDataRepository

    @Binds
    @Singleton
    fun bindEnqueuedRecognitionRepository(implementation: EnqueuedRecognitionRepositoryImpl): EnqueuedRecognitionDataRepository

    @Binds
    @Singleton
    fun bindFileRecordRepository(implementation: FileRecordRepositoryImpl): FileRecordDataRepository

}