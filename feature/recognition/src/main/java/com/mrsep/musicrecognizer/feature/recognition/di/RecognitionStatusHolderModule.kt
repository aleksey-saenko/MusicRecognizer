package com.mrsep.musicrecognizer.feature.recognition.di

import com.mrsep.musicrecognizer.feature.recognition.MutableRecognitionStatusHolder
import com.mrsep.musicrecognizer.feature.recognition.RecognitionStatusHolder
import com.mrsep.musicrecognizer.feature.recognition.RecognitionStatusHolderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
internal interface RecognitionStatusHolderModule {

    @MainScreenStatusHolder
    @Binds
    @Singleton
    fun bindScreenMutableHolder(impl: RecognitionStatusHolderImpl):
            MutableRecognitionStatusHolder

    @MainScreenStatusHolder
    @Binds
    fun bindScreenHolder(@MainScreenStatusHolder impl: MutableRecognitionStatusHolder):
            RecognitionStatusHolder

    @WidgetStatusHolder
    @Binds
    @Singleton
    fun bindWidgetMutableHolder(impl: RecognitionStatusHolderImpl):
            MutableRecognitionStatusHolder

    @WidgetStatusHolder
    @Binds
    fun bindWidgetHolder(@WidgetStatusHolder impl: MutableRecognitionStatusHolder):
            RecognitionStatusHolder
}

@Retention(AnnotationRetention.BINARY)
@Qualifier
internal annotation class MainScreenStatusHolder

@Retention(AnnotationRetention.BINARY)
@Qualifier
internal annotation class WidgetStatusHolder
