package com.mrsep.musicrecognizer.feature.recognition.di

import com.mrsep.musicrecognizer.feature.recognition.presentation.service.ScheduledResultNotificationHelper
import com.mrsep.musicrecognizer.feature.recognition.presentation.service.ScheduledResultNotificationHelperImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
internal interface NotificationHelperModule {

    @Binds
    @Singleton
    fun bindScheduledResultNotificationHelper(
        implementation: ScheduledResultNotificationHelperImpl
    ): ScheduledResultNotificationHelper
}
