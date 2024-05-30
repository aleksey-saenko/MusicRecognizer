package com.mrsep.musicrecognizer.feature.recognition.di

import com.mrsep.musicrecognizer.feature.recognition.domain.EnqueuedRecognitionScheduler
import com.mrsep.musicrecognizer.feature.recognition.domain.RecognitionInteractor
import com.mrsep.musicrecognizer.feature.recognition.domain.TrackMetadataEnhancerScheduler
import com.mrsep.musicrecognizer.feature.recognition.domain.impl.RecognitionInteractorImpl
import com.mrsep.musicrecognizer.feature.recognition.scheduler.EnqueuedRecognitionSchedulerImpl
import com.mrsep.musicrecognizer.feature.recognition.platform.TrackMetadataEnhancerSchedulerImpl
import com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen.ScreenRecognitionController
import com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen.ScreenRecognitionControllerImpl
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
    fun bindRecognitionInteractor(implementation: RecognitionInteractorImpl):
//    fun bindRecognitionInteractor(implementation: RecognitionInteractorFakeImpl):
            RecognitionInteractor

    @Binds
    @Singleton
    fun bindScreenRecognitionController(implementation: ScreenRecognitionControllerImpl):
            ScreenRecognitionController

    @Binds
    @Singleton
    fun bindRecognitionScheduler(implementation: EnqueuedRecognitionSchedulerImpl):
            EnqueuedRecognitionScheduler

    @Binds
    fun bindTrackMetadataEnhancerScheduler(implementation: TrackMetadataEnhancerSchedulerImpl):
            TrackMetadataEnhancerScheduler
}