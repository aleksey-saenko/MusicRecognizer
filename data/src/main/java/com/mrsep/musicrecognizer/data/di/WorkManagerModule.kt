package com.mrsep.musicrecognizer.data.di

import com.mrsep.musicrecognizer.data.enqueued.EnqueuedRecognitionWorkManager
import com.mrsep.musicrecognizer.data.enqueued.EnqueuedRecognitionWorkManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface WorkManagerModule {

    @Binds
    @Singleton
    fun bindWorkDataManager(implementation: EnqueuedRecognitionWorkManagerImpl): EnqueuedRecognitionWorkManager

}