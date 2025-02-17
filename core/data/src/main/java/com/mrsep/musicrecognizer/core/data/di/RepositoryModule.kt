package com.mrsep.musicrecognizer.core.data.di

import com.mrsep.musicrecognizer.core.data.ConnectivityManagerNetworkMonitor
import com.mrsep.musicrecognizer.core.data.enqueued.EnqueuedRecognitionRepositoryImpl
import com.mrsep.musicrecognizer.core.data.enqueued.RecordingFileDataSource
import com.mrsep.musicrecognizer.core.data.enqueued.RecordingFileDataSourceImpl
import com.mrsep.musicrecognizer.core.data.preferences.PreferencesRepositoryImpl
import com.mrsep.musicrecognizer.core.data.track.TrackRepositoryImpl
import com.mrsep.musicrecognizer.core.domain.preferences.PreferencesRepository
import com.mrsep.musicrecognizer.core.domain.recognition.EnqueuedRecognitionRepository
import com.mrsep.musicrecognizer.core.domain.track.TrackRepository
import com.mrsep.musicrecognizer.core.domain.recognition.NetworkMonitor
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
    fun bindTrackRepository(impl: TrackRepositoryImpl): TrackRepository

    @Binds
    @Singleton
    fun bindPreferencesRepository(impl: PreferencesRepositoryImpl): PreferencesRepository


    @Binds
    @Singleton
    fun bindRecognitionRepository(impl: EnqueuedRecognitionRepositoryImpl): EnqueuedRecognitionRepository

    @Binds
    @Singleton
    fun bindRecordingDataSource(impl: RecordingFileDataSourceImpl): RecordingFileDataSource

    @Binds
    @Singleton
    fun bindNetworkMonitor(impl: ConnectivityManagerNetworkMonitor): NetworkMonitor
}