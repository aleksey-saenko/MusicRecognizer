package com.mrsep.musicrecognizer.glue.recognitionqueue.di

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionDo
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionStatusDo
import com.mrsep.musicrecognizer.data.player.PlayerStatusDo
import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionResultDo
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
    fun bindEnqueuedStatusMapper(implementation: EnqueuedStatusMapper):
            Mapper<EnqueuedRecognitionStatusDo, EnqueuedRecognitionStatus>

    @Binds
    fun bindEnqueuedMapper(implementation: EnqueuedMapper):
            Mapper<EnqueuedRecognitionDo, EnqueuedRecognition>

    @Binds
    fun bindPlayerStatusMapper(implementation: PlayerStatusMapper):
            Mapper<PlayerStatusDo, PlayerStatus>

    @Binds
    fun bindTrackMapper(implementation: TrackMapper): Mapper<TrackEntity, Track>

    @Binds
    fun bindRemoteResultMapper(implementation: RemoteResultMapper):
            Mapper<RemoteRecognitionResultDo, RemoteRecognitionResult>

}