package com.mrsep.musicrecognizer.data.di

import com.mrsep.musicrecognizer.data.remote.audd.rest.AuddRecognitionServicePure
import com.mrsep.musicrecognizer.data.remote.audd.rest.RecognitionServiceDo
import com.mrsep.musicrecognizer.data.remote.audd.websocket.RecognitionStreamServiceDo
import com.mrsep.musicrecognizer.data.remote.audd.websocket.RecognitionStreamServiceImpl
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
//    fun bindRecognitionService(implementation: AuddRecognitionService): RecognitionDataService
    fun bindRecognitionService(implementation: AuddRecognitionServicePure): RecognitionServiceDo

    @Binds
    @Singleton
//    fun bindRemoteRecognitionStreamService(implementation: AuddRestStreamServiceImpl): RecognitionStreamDataService
    fun bindRemoteRecognitionStreamService(implementation: RecognitionStreamServiceImpl): RecognitionStreamServiceDo

}