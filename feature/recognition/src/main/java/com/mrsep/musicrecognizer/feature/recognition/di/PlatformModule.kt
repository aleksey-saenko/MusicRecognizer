package com.mrsep.musicrecognizer.feature.recognition.di

import com.mrsep.musicrecognizer.feature.recognition.domain.VibrationManager
import com.mrsep.musicrecognizer.feature.recognition.platform.VibrationManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
internal interface PlatformModule {

    @Binds
    @Singleton
    fun bindVibrationManager(implementation: VibrationManagerImpl): VibrationManager
}
