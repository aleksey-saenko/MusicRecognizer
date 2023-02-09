package com.mrsep.musicrecognizer.di

import com.mrsep.musicrecognizer.data.track.TrackEntity
import com.mrsep.musicrecognizer.data.track.TrackToDataMapper
import com.mrsep.musicrecognizer.data.track.TrackToDomainMapper
import com.mrsep.musicrecognizer.domain.model.Mapper
import com.mrsep.musicrecognizer.domain.model.Track
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface MapperModule {

    @Binds
    fun provideTrackToDomainMapper(implementation: TrackToDomainMapper): Mapper<TrackEntity, Track>

    @Binds
    fun provideTrackToDataMapper(implementation: TrackToDataMapper): Mapper<Track, TrackEntity>

}