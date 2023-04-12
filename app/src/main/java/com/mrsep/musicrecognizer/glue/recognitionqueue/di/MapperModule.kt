package com.mrsep.musicrecognizer.glue.recognitionqueue.di

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionDataStatus
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionEntityWithStatus
import com.mrsep.musicrecognizer.data.player.PlayerDataStatus
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.model.EnqueuedRecognitionStatus
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.model.EnqueuedRecognitionWithStatus
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.model.PlayerStatus
import com.mrsep.musicrecognizer.glue.recognitionqueue.mapper.*
import com.mrsep.musicrecognizer.glue.recognitionqueue.mapper.EnqueuedStatusToDomainMapper
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface MapperModule {

    @Binds
    fun bindEnqueuedStatusToDomainMapper(implementation: EnqueuedStatusToDomainMapper):
            Mapper<EnqueuedRecognitionDataStatus, EnqueuedRecognitionStatus>

    @Binds
    fun bindEnqueuedToDomainMapper(implementation: EnqueuedToDomainMapper):
            Mapper<EnqueuedRecognitionEntityWithStatus, EnqueuedRecognitionWithStatus>

    @Binds
    fun bindPlayerStatusToDomainMapper(implementation: PlayerStatusToDomainMapper):
            Mapper<PlayerDataStatus, PlayerStatus>

}