package com.mrsep.musicrecognizer.glue.preferences.di

import com.mrsep.musicrecognizer.feature.preferences.domain.PreferencesRepository
import com.mrsep.musicrecognizer.glue.preferences.adapter.AdapterPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface AdapterModule {

    @Binds
    fun bindPreferencesRepository(implementation: AdapterPreferencesRepository): PreferencesRepository

}