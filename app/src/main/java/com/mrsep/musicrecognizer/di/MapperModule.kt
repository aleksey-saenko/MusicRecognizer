package com.mrsep.musicrecognizer.di

import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.UserPreferencesProto.RequiredServicesProto
import com.mrsep.musicrecognizer.data.enqueued.EnqueuedRecognitionEntity
import com.mrsep.musicrecognizer.data.enqueued.EnqueuedToDataMapper
import com.mrsep.musicrecognizer.data.enqueued.EnqueuedToDomainMapper
import com.mrsep.musicrecognizer.data.preferences.PreferencesToDomainMapper
import com.mrsep.musicrecognizer.data.preferences.RequiredServicesToProtoMapper
import com.mrsep.musicrecognizer.data.track.TrackEntity
import com.mrsep.musicrecognizer.data.track.TrackToDataMapper
import com.mrsep.musicrecognizer.data.track.TrackToDomainMapper
import com.mrsep.musicrecognizer.domain.model.EnqueuedRecognition
import com.mrsep.musicrecognizer.domain.model.Mapper
import com.mrsep.musicrecognizer.domain.model.Track
import com.mrsep.musicrecognizer.domain.model.UserPreferences
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface MapperModule {

    @Binds
    fun provideTrackToDomainMapper(implementation: TrackToDomainMapper):
            Mapper<TrackEntity, Track>
    @Binds
    fun provideTrackToDataMapper(implementation: TrackToDataMapper):
            Mapper<Track, TrackEntity>


    @Binds
    fun providePreferencesToDomainMapper(implementation: PreferencesToDomainMapper):
            Mapper<UserPreferencesProto, UserPreferences>
    @Binds
    fun provideRequiredServicesToProtoMapper(implementation: RequiredServicesToProtoMapper):
            Mapper<UserPreferences.RequiredServices, RequiredServicesProto>


    @Binds
    fun provideEnqueuedToDomainMapper(implementation: EnqueuedToDomainMapper):
            Mapper<EnqueuedRecognitionEntity, EnqueuedRecognition>
    @Binds
    fun provideEnqueuedToDataMapper(implementation: EnqueuedToDataMapper):
            Mapper<EnqueuedRecognition, EnqueuedRecognitionEntity>

}