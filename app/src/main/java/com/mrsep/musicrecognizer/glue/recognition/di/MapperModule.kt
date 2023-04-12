package com.mrsep.musicrecognizer.glue.recognition.di

import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.recorder.RecordDataResult
import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionDataResult
import com.mrsep.musicrecognizer.data.track.TrackEntity
import com.mrsep.musicrecognizer.feature.recognition.domain.RecordResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RemoteRecognitionResult
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
    fun bindPreferencesToDomainMapper(implementation: PreferencesToDomainMapper):
            Mapper<UserPreferencesProto, UserPreferences>
    @Binds
    fun bindTrackToDomainMapper(implementation: TrackToDomainMapper):
            Mapper<TrackEntity, Track>

    @Binds
    fun bindTrackToDataMapper(implementation: TrackToDataMapper):
            Mapper<Track, TrackEntity>

    @Binds
    fun bindRemoteTrackResultToDomainMapper(implementation: RemoteTrackResultToDomainMapper):
            Mapper<RemoteRecognitionDataResult<Track>, RemoteRecognitionResult<Track>>

    @Binds
    fun bindRequiredServicesToProtoMapper(implementation: RequiredServicesToProtoMapper):
            Mapper<UserPreferences.RequiredServices, UserPreferencesProto.RequiredServicesProto>

    @Binds
    fun bindRecordResultToDomainMapper(implementation: RecordResultToDomainMapper):
            Mapper<RecordDataResult, RecordResult>


}