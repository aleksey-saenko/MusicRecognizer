package com.mrsep.musicrecognizer.glue.recognition.di

import com.mrsep.musicrecognizer.feature.recognition.presentation.service.RecognitionControlServiceRouter
import com.mrsep.musicrecognizer.glue.recognition.impl.RecognitionControlServiceRouterImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface RecognitionControlServiceModule {

    @Binds
    fun bindRecognitionControlServiceRouter(impl: RecognitionControlServiceRouterImpl):
            RecognitionControlServiceRouter
}
