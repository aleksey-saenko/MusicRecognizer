package com.mrsep.musicrecognizer.glue.onboarding.di

import com.mrsep.musicrecognizer.feature.onboarding.domain.PreferencesRepository
import com.mrsep.musicrecognizer.feature.onboarding.domain.RecognitionService
import com.mrsep.musicrecognizer.glue.onboarding.adapter.AdapterPreferencesRepository
import com.mrsep.musicrecognizer.glue.onboarding.adapter.AdapterRecognitionService
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

    @Binds
    fun bindAdapterRecognitionService(implementation: AdapterRecognitionService): RecognitionService

}