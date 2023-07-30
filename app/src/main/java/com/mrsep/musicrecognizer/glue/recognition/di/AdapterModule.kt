package com.mrsep.musicrecognizer.glue.recognition.di

import com.mrsep.musicrecognizer.feature.recognition.domain.*
import com.mrsep.musicrecognizer.glue.recognition.adapters.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface AdapterModule {

    @Binds
    fun bindEnqueuedRecognitionRepository(implementation: AdapterEnqueuedRepository): EnqueuedRecognitionRepository

    @Binds
    fun bindPreferencesRepository(implementation: AdapterPreferencesRepository): PreferencesRepository

    @Binds
    fun bindTrackRepository(implementation: AdapterTrackRepository): TrackRepository

    @Binds
    fun bindRecognitionService(implementation: AdapterRecognitionService): RemoteRecognitionService

    @Binds
    fun bindRecorderController(implementation: AdapterRecorderController): AudioRecorderController

    @Binds
    fun bindPlayerController(implementation: AdapterPlayerController): PlayerController

    @Binds
    fun bindNetworkMonitor(implementation: AdapterNetworkMonitor): NetworkMonitor

}