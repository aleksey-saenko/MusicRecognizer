package com.mrsep.musicrecognizer.data.di

import com.mrsep.musicrecognizer.data.remote.audd.rest.AuddRecognitionService
import com.mrsep.musicrecognizer.data.remote.audd.rest.RecognitionDataService
import com.mrsep.musicrecognizer.data.remote.audd.websocket.RecognitionStreamDataService
import com.mrsep.musicrecognizer.data.remote.audd.websocket.RecognitionStreamDataServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface RecognitionModule {

    @Binds
    @Singleton
    fun bindRecognitionService(implementation: AuddRecognitionService): RecognitionDataService

    @Binds
    @Singleton
//    fun bindRemoteRecognitionStreamService(implementation: AuddRestStreamServiceImpl): RecognitionStreamDataService
    fun bindRemoteRecognitionStreamService(implementation: RecognitionStreamDataServiceImpl): RecognitionStreamDataService

}