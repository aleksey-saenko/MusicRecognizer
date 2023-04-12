package com.mrsep.musicrecognizer.glue.recognition.di

import com.mrsep.musicrecognizer.feature.recognition.presentation.service.NotificationServiceRouter
import com.mrsep.musicrecognizer.glue.recognition.impl.NotificationServiceRouterImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface NotificationServiceModule {

    @Binds
    fun bindNotificationServiceRouter(implementation: NotificationServiceRouterImpl): NotificationServiceRouter

}