package com.mrsep.musicrecognizer.glue.library.di

import com.mrsep.musicrecognizer.feature.library.domain.repository.PreferencesRepository
import com.mrsep.musicrecognizer.feature.library.domain.repository.TrackRepository
import com.mrsep.musicrecognizer.glue.library.adapter.AdapterPreferencesRepository
import com.mrsep.musicrecognizer.glue.library.adapter.AdapterTrackRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface AdapterModule {

    @Binds
    fun bindTrackRepository(implementation: AdapterTrackRepository):
            TrackRepository

    @Binds
    fun bindPreferencesRepository(implementation: AdapterPreferencesRepository):
            PreferencesRepository

}