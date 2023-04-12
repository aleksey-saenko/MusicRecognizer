package com.mrsep.musicrecognizer.data.di

import com.mrsep.musicrecognizer.data.remote.audd.AuddRecognitionService
import com.mrsep.musicrecognizer.data.remote.audd.RecognitionDataService
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

}