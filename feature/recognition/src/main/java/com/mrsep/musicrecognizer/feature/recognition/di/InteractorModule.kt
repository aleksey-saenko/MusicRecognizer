package com.mrsep.musicrecognizer.feature.recognition.di

import com.mrsep.musicrecognizer.feature.recognition.domain.RecognitionInteractor
import com.mrsep.musicrecognizer.feature.recognition.domain.impl.RecognitionInteractorFakeImpl
import com.mrsep.musicrecognizer.feature.recognition.domain.impl.RecognitionInteractorImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface InteractorModule {

    @Binds
    @Singleton
    fun bindRecognitionInteractor(implementation: RecognitionInteractorImpl): RecognitionInteractor

}