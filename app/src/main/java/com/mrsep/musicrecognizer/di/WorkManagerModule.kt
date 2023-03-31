package com.mrsep.musicrecognizer.di

import com.mrsep.musicrecognizer.data.enqueued.EnqueuedRecognitionWorkManagerImpl
import com.mrsep.musicrecognizer.domain.EnqueuedRecognitionWorkManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface WorkManagerModule {

    @Binds
    fun bindEnqueuedRecognitionWorkManager(implementation: EnqueuedRecognitionWorkManagerImpl)
            : EnqueuedRecognitionWorkManager

}