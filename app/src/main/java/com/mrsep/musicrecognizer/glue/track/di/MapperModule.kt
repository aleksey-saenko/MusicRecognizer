package com.mrsep.musicrecognizer.glue.track.di

import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.track.TrackEntity
import com.mrsep.musicrecognizer.feature.track.domain.model.Track
import com.mrsep.musicrecognizer.feature.track.domain.model.UserPreferences
import com.mrsep.musicrecognizer.glue.track.mapper.*
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

}