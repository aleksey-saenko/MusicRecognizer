package com.mrsep.musicrecognizer.di

import com.mrsep.musicrecognizer.feature.backup.AppRestartManager
import com.mrsep.musicrecognizer.feature.preferences.RecognitionServiceStarter
import com.mrsep.musicrecognizer.feature.recognition.DeeplinkRouter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface GlueModule {

    @Binds
    fun bindAppRestartManager(impl: AndroidAppRestarter): AppRestartManager

    @Binds
    fun bindDeeplinkRouter(impl: AppDeeplinkRouter): DeeplinkRouter

    @Binds
    fun bindRecognitionServiceStarter(impl: ServiceStarter): RecognitionServiceStarter
}