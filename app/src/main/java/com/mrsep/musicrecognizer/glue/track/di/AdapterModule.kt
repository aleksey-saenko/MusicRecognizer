package com.mrsep.musicrecognizer.glue.track.di

import com.mrsep.musicrecognizer.feature.track.domain.PreferencesRepository
import com.mrsep.musicrecognizer.feature.track.domain.TrackMetadataEnhancerScheduler
import com.mrsep.musicrecognizer.feature.track.domain.TrackRepository
import com.mrsep.musicrecognizer.glue.track.adapter.AdapterPreferencesRepository
import com.mrsep.musicrecognizer.glue.track.adapter.AdapterTrackMetadataEnhancerScheduler
import com.mrsep.musicrecognizer.glue.track.adapter.AdapterTrackRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface AdapterModule {

    @Binds
    fun bindTrackRepository(implementation: AdapterTrackRepository): TrackRepository

    @Binds
    fun bindPreferencesRepository(implementation: AdapterPreferencesRepository): PreferencesRepository

    @Binds
    fun bindMetadataEnhancerScheduler(implementation: AdapterTrackMetadataEnhancerScheduler):
            TrackMetadataEnhancerScheduler
}
