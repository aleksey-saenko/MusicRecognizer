package com.mrsep.musicrecognizer.feature.recognition.di

import com.mrsep.musicrecognizer.core.domain.recognition.EnqueuedRecognitionScheduler
import com.mrsep.musicrecognizer.core.domain.recognition.RecognitionInteractor
import com.mrsep.musicrecognizer.core.domain.recognition.TrackMetadataEnhancerScheduler
import com.mrsep.musicrecognizer.feature.recognition.domain.RecognitionInteractorImpl
import com.mrsep.musicrecognizer.feature.recognition.scheduler.TrackMetadataEnhancerSchedulerImpl
import com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen.ScreenRecognitionController
import com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen.ScreenRecognitionControllerImpl
import com.mrsep.musicrecognizer.feature.recognition.scheduler.EnqueuedRecognitionSchedulerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
internal interface RecognitionModule {

    @Binds
    @Singleton
    fun bindRecognitionInteractor(impl: RecognitionInteractorImpl): RecognitionInteractor

    @Binds
    @Singleton
    fun bindScreenRecognitionController(impl: ScreenRecognitionControllerImpl):
            ScreenRecognitionController

    @Binds
    @Singleton
    fun bindRecognitionScheduler(impl: EnqueuedRecognitionSchedulerImpl):
            EnqueuedRecognitionScheduler

    @Binds
    @Singleton
    fun bindTrackMetadataEnhancerScheduler(impl: TrackMetadataEnhancerSchedulerImpl):
            TrackMetadataEnhancerScheduler
}
