package com.mrsep.musicrecognizer.glue.recognitionqueue.di

import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.EnqueuedRecognitionRepository
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.FileRecordRepository
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.PlayerController
import com.mrsep.musicrecognizer.glue.recognitionqueue.adapters.*
import com.mrsep.musicrecognizer.glue.recognitionqueue.adapters.AdapterEnqueuedRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface AdapterModule {

    @Binds
    fun bindEnqueuedRecognitionRepository(implementation: AdapterEnqueuedRepository): EnqueuedRecognitionRepository

    @Binds
    fun bindPlayerController(implementation: AdapterPlayerController): PlayerController

    @Binds
    fun bindFileRecordRepository(implementation: AdapterFileRecordRepository): FileRecordRepository

}