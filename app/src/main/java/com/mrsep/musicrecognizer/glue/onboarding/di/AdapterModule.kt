package com.mrsep.musicrecognizer.glue.onboarding.di

import com.mrsep.musicrecognizer.feature.onboarding.domain.ConfigValidator
import com.mrsep.musicrecognizer.feature.onboarding.domain.PreferencesRepository
import com.mrsep.musicrecognizer.glue.onboarding.adapter.AdapterConfigValidator
import com.mrsep.musicrecognizer.glue.onboarding.adapter.AdapterPreferencesRepository
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
    fun bindAdapterConfigValidator(implementation: AdapterConfigValidator): ConfigValidator
}
