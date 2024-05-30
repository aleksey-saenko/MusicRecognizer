package com.mrsep.musicrecognizer.feature.recognition.di

import com.mrsep.musicrecognizer.feature.recognition.domain.impl.MutableRecognitionStatusHolder
import com.mrsep.musicrecognizer.feature.recognition.domain.impl.RecognitionStatusHolder
import com.mrsep.musicrecognizer.feature.recognition.domain.impl.RecognitionStatusHolderImpl
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
    fun bindScreenMutableHolder(implementation: RecognitionStatusHolderImpl):
            MutableRecognitionStatusHolder

    @MainScreenStatusHolder
    @Binds
    fun bindScreenHolder(@MainScreenStatusHolder implementation: MutableRecognitionStatusHolder):
            RecognitionStatusHolder

    @WidgetStatusHolder
    @Binds
    @Singleton
    fun bindWidgetMutableHolder(implementation: RecognitionStatusHolderImpl):
            MutableRecognitionStatusHolder

    @WidgetStatusHolder
    @Binds
    fun bindWidgetHolder(@WidgetStatusHolder implementation: MutableRecognitionStatusHolder):
            RecognitionStatusHolder

}

@Retention(AnnotationRetention.BINARY)
@Qualifier
internal annotation class MainScreenStatusHolder

@Retention(AnnotationRetention.BINARY)
@Qualifier
internal annotation class WidgetStatusHolder