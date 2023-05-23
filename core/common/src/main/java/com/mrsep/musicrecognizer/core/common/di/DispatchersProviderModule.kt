package com.mrsep.musicrecognizer.core.common.di

import com.mrsep.musicrecognizer.core.common.DefaultDispatchersProvider
import com.mrsep.musicrecognizer.core.common.DispatchersProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface DispatchersProviderModule {

    @Binds
    fun bindDispatchersProvider(implementation: DefaultDispatchersProvider): DispatchersProvider

}