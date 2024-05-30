package com.mrsep.musicrecognizer.feature.recognition.widget

import com.mrsep.musicrecognizer.feature.recognition.di.WidgetStatusHolder
import com.mrsep.musicrecognizer.feature.recognition.domain.impl.RecognitionStatusHolder
import com.mrsep.musicrecognizer.feature.recognition.presentation.service.NotificationServiceRouter
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface RecognitionWidgetEntryPoint {

    fun serviceRouter(): NotificationServiceRouter

    @WidgetStatusHolder
    fun widgetStatusHolder(): RecognitionStatusHolder
}