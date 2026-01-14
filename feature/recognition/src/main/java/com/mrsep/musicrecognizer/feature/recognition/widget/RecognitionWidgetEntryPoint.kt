package com.mrsep.musicrecognizer.feature.recognition.widget

import com.mrsep.musicrecognizer.core.domain.preferences.PreferencesRepository
import com.mrsep.musicrecognizer.feature.recognition.di.WidgetStatusHolder
import com.mrsep.musicrecognizer.feature.recognition.RecognitionStatusHolder
import com.mrsep.musicrecognizer.feature.recognition.DeeplinkRouter
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface RecognitionWidgetEntryPoint {

    fun serviceRouter(): DeeplinkRouter

    @WidgetStatusHolder
    fun widgetStatusHolder(): RecognitionStatusHolder

    fun preferencesRepository(): PreferencesRepository
}
