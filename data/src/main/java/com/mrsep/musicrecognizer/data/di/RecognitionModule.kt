package com.mrsep.musicrecognizer.data.di

import com.mrsep.musicrecognizer.data.remote.audd.AuddRecognitionService
import com.mrsep.musicrecognizer.data.remote.audd.AuddRestStreamServiceImpl
import com.mrsep.musicrecognizer.data.remote.audd.RecognitionDataService
import com.mrsep.musicrecognizer.data.remote.audd.websocket.RecognitionStreamDataService
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
    fun bindRemoteRecognitionStreamService(implementation: AuddRestStreamServiceImpl): RecognitionStreamDataService

}