package com.mrsep.musicrecognizer.glue.recognitionqueue.di

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionData
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionDataStatus
import com.mrsep.musicrecognizer.data.player.PlayerDataStatus
import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionDataResult
import com.mrsep.musicrecognizer.data.track.TrackEntity
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.model.EnqueuedRecognitionStatus
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.model.EnqueuedRecognition
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.model.PlayerStatus
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.model.Track
import com.mrsep.musicrecognizer.glue.recognitionqueue.mapper.*
import com.mrsep.musicrecognizer.glue.recognitionqueue.mapper.EnqueuedStatusMapper
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface MapperModule {

    @Binds
    fun bindEnqueuedStatusToDomainMapper(implementation: EnqueuedStatusMapper):
            Mapper<EnqueuedRecognitionDataStatus, EnqueuedRecognitionStatus>

    @Binds
    fun bindEnqueuedToDomainMapper(implementation: EnqueuedMapper):
            Mapper<EnqueuedRecognitionData, EnqueuedRecognition>

    @Binds
    fun bindPlayerStatusToDomainMapper(implementation: PlayerStatusMapper):
            Mapper<PlayerDataStatus, PlayerStatus>

    @Binds
    fun bindTrackMapper(implementation: TrackMapper): Mapper<TrackEntity, Track>

    @Binds
    fun bindRemoteResultMapper(implementation: RemoteResultMapper):
            Mapper<RemoteRecognitionDataResult, RemoteRecognitionResult>

}