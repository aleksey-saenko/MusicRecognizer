package com.mrsep.musicrecognizer.di

import com.mrsep.musicrecognizer.data.enqueued.EnqueuedRecognitionRepositoryImpl
import com.mrsep.musicrecognizer.data.preferences.PreferencesRepositoryImpl
import com.mrsep.musicrecognizer.data.recorder.FileRecordRepositoryImpl
import com.mrsep.musicrecognizer.data.track.TrackRepositoryImpl
import com.mrsep.musicrecognizer.domain.EnqueuedRecognitionRepository
import com.mrsep.musicrecognizer.domain.FileRecordRepository
import com.mrsep.musicrecognizer.domain.PreferencesRepository
import com.mrsep.musicrecognizer.domain.TrackRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    fun bindTrackRepository(implementation: TrackRepositoryImpl): TrackRepository

    @Binds
    fun bindPreferencesRepository(implementation: PreferencesRepositoryImpl): PreferencesRepository

    @Binds
    fun bindEnqueuedRecognitionRepository(implementation: EnqueuedRecognitionRepositoryImpl): EnqueuedRecognitionRepository

    @Binds
    fun bindFileRecordRepository(implementation: FileRecordRepositoryImpl): FileRecordRepository

}