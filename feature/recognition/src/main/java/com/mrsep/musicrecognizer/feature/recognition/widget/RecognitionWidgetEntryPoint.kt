package com.mrsep.musicrecognizer.feature.recognition.widget

import com.mrsep.musicrecognizer.feature.recognition.di.WidgetStatusHolder
import com.mrsep.musicrecognizer.feature.recognition.domain.impl.RecognitionStatusHolder
import com.mrsep.musicrecognizer.feature.recognition.presentation.service.RecognitionControlServiceRouter
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface RecognitionWidgetEntryPoint {

    fun serviceRouter(): RecognitionControlServiceRouter

    @WidgetStatusHolder
    fun widgetStatusHolder(): RecognitionStatusHolder
}
