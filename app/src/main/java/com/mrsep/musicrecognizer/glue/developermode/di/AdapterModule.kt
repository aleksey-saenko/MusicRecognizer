package com.mrsep.musicrecognizer.glue.developermode.di

import com.mrsep.musicrecognizer.feature.developermode.domain.DatabaseInteractor
import com.mrsep.musicrecognizer.glue.developermode.adapter.AdapterDatabaseInteractor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface AdapterModule {

    @Binds
    fun bindDatabaseInteractor(implementation: AdapterDatabaseInteractor) : DatabaseInteractor

}