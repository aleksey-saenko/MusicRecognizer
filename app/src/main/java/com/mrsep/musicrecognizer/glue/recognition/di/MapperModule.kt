package com.mrsep.musicrecognizer.glue.recognition.di

import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.UserPreferencesProto.ScheduleActionProto
import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.audiorecord.AudioRecordingDataStrategy
import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionDataResult
import com.mrsep.musicrecognizer.data.track.TrackEntity
import com.mrsep.musicrecognizer.feature.recognition.domain.model.AudioRecordingStrategy
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.ScheduleAction
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
            Mapper<UserPreferencesProto, UserPreferences>
    @Binds
    fun bindTrackMapper(implementation: TrackMapper): BidirectionalMapper<TrackEntity, Track>

    @Binds
    fun bindRemoteResultMapper(implementation: RemoteResultMapper):
            Mapper<RemoteRecognitionDataResult, RemoteRecognitionResult>

    @Binds
    fun bindRequiredServicesMapper(implementation: RequiredServicesMapper):
            Mapper<UserPreferences.RequiredServices, UserPreferencesProto.RequiredServicesProto>

    @Binds
    fun bindAudioRecordingMapper(implementation: AudioRecordingMapper):
            Mapper<AudioRecordingStrategy, AudioRecordingDataStrategy>

    @Binds
    fun bindScheduleActionMapper(implementation: ScheduleActionMapper):
            Mapper<ScheduleActionProto, ScheduleAction>

}