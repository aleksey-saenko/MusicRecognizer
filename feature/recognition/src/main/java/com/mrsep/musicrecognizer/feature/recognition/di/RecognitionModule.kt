package com.mrsep.musicrecognizer.feature.recognition.di

import com.mrsep.musicrecognizer.feature.recognition.domain.EnqueuedRecognitionScheduler
import com.mrsep.musicrecognizer.feature.recognition.domain.TrackMetadataEnhancerScheduler
import com.mrsep.musicrecognizer.feature.recognition.domain.ScreenRecognitionInteractor
import com.mrsep.musicrecognizer.feature.recognition.domain.ServiceRecognitionInteractor
import com.mrsep.musicrecognizer.feature.recognition.domain.impl.RecognitionInteractorFakeImpl
import com.mrsep.musicrecognizer.feature.recognition.domain.impl.RecognitionInteractorImpl
import com.mrsep.musicrecognizer.feature.recognition.scheduler.EnqueuedRecognitionSchedulerImpl
import com.mrsep.musicrecognizer.feature.recognition.platform.TrackMetadataEnhancerSchedulerImpl
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
    fun bindScreenRecognitionInteractor(implementation: RecognitionInteractorImpl):
//    fun bindScreenRecognitionInteractor(implementation: RecognitionInteractorFakeImpl):
            ScreenRecognitionInteractor

    @Binds
    @Singleton
    fun bindServiceRecognitionInteractor(implementation: RecognitionInteractorImpl):
//    fun bindServiceRecognitionInteractor(implementation: RecognitionInteractorFakeImpl):
            ServiceRecognitionInteractor

    @Binds
    @Singleton
    fun bindRecognitionScheduler(implementation: EnqueuedRecognitionSchedulerImpl):
            EnqueuedRecognitionScheduler

    @Binds
    fun bindTrackMetadataEnhancerScheduler(implementation: TrackMetadataEnhancerSchedulerImpl):
            TrackMetadataEnhancerScheduler

}