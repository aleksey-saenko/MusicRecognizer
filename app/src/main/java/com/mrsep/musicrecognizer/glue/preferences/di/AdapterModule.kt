package com.mrsep.musicrecognizer.glue.preferences.di

import com.mrsep.musicrecognizer.feature.preferences.domain.AppBackupManager
import com.mrsep.musicrecognizer.feature.preferences.domain.PreferencesRepository
import com.mrsep.musicrecognizer.feature.preferences.domain.PreferencesRouter
import com.mrsep.musicrecognizer.glue.preferences.adapter.AdapterAppBackupManager
import com.mrsep.musicrecognizer.glue.preferences.adapter.AdapterPreferencesRepository
import com.mrsep.musicrecognizer.glue.preferences.adapter.AdapterPreferencesRouter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface AdapterModule {

    @Binds
    fun bindPreferencesRepository(impl: AdapterPreferencesRepository): PreferencesRepository

    @Binds
    fun bindPreferencesRouter(impl: AdapterPreferencesRouter): PreferencesRouter

    @Binds
    fun bindAppBackupManager(impl: AdapterAppBackupManager): AppBackupManager
}
