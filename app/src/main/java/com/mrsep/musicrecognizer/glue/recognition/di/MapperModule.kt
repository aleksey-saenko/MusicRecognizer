package com.mrsep.musicrecognizer.glue.recognition.di

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.audiorecord.RecognitionSchemeDo
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionEntityWithTrack
import com.mrsep.musicrecognizer.data.audioplayer.PlayerStatusDo
import com.mrsep.musicrecognizer.data.preferences.FallbackActionDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import com.mrsep.musicrecognizer.data.remote.AcrCloudConfigDo
import com.mrsep.musicrecognizer.data.remote.AuddConfigDo
import com.mrsep.musicrecognizer.data.remote.RecognitionProviderDo
import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionResultDo
import com.mrsep.musicrecognizer.data.remote.enhancer.RemoteMetadataEnhancingResultDo
import com.mrsep.musicrecognizer.data.track.MusicServiceDo
import com.mrsep.musicrecognizer.data.track.TrackEntity
import com.mrsep.musicrecognizer.feature.recognition.domain.model.AcrCloudConfig
import com.mrsep.musicrecognizer.feature.recognition.domain.model.AuddConfig
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionScheme
import com.mrsep.musicrecognizer.feature.recognition.domain.model.EnqueuedRecognition
import com.mrsep.musicrecognizer.feature.recognition.domain.model.PlayerStatus
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.FallbackAction
import com.mrsep.musicrecognizer.feature.recognition.domain.model.MusicService
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionProvider
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RemoteMetadataEnhancingResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.Track
import com.mrsep.musicrecognizer.feature.recognition.domain.model.UserPreferences
import com.mrsep.musicrecognizer.glue.recognition.mapper.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface MapperModule {

    @Binds
    fun bindUserPreferencesMapper(implementation: UserPreferencesMapper):
            Mapper<UserPreferencesDo, UserPreferences>

    @Binds
    fun bindTrackMapper(implementation: TrackMapper): BidirectionalMapper<TrackEntity, Track>

    @Binds
    fun bindRemoteRecognitionResultMapper(implementation: RemoteRecognitionResultMapper):
            Mapper<RemoteRecognitionResultDo, RemoteRecognitionResult>

    @Binds
    fun bindAudioRecordingMapper(implementation: AudioRecordingMapper):
            Mapper<RecognitionScheme, RecognitionSchemeDo>

    @Binds
    fun bindFallbackActionMapper(implementation: FallbackActionMapper):
            Mapper<FallbackActionDo, FallbackAction>

    @Binds
    fun bindPlayerStatusMapper(implementation: PlayerStatusMapper):
            Mapper<PlayerStatusDo, PlayerStatus>

    @Binds
    fun bindEnqueuedRecognitionMapper(implementation: EnqueuedRecognitionMapper):
            BidirectionalMapper<EnqueuedRecognitionEntityWithTrack, EnqueuedRecognition>

    @Binds
    fun bindRemoteEnhancingResultMapper(implementation: RemoteEnhancingResultMapper):
            Mapper<RemoteMetadataEnhancingResultDo, RemoteMetadataEnhancingResult>

    @Binds
    fun bindRecognitionProviderMapper(implementation: RecognitionProviderMapper):
            BidirectionalMapper<RecognitionProviderDo, RecognitionProvider>

    @Binds
    fun bindAuddConfigMapper(implementation: AuddConfigMapper):
            BidirectionalMapper<AuddConfigDo, AuddConfig>

    @Binds
    fun bindAcrCloudConfigMapper(implementation: AcrCloudConfigMapper):
            BidirectionalMapper<AcrCloudConfigDo, AcrCloudConfig>

}