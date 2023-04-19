package com.mrsep.musicrecognizer.glue.library.di

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.track.SearchDataResult
import com.mrsep.musicrecognizer.data.track.TrackDataFilter
import com.mrsep.musicrecognizer.data.track.TrackEntity
import com.mrsep.musicrecognizer.feature.library.domain.model.SearchResult
import com.mrsep.musicrecognizer.feature.library.domain.model.Track
import com.mrsep.musicrecognizer.feature.library.domain.model.TrackFilter
import com.mrsep.musicrecognizer.glue.library.mapper.SearchResultToDomainMapper
import com.mrsep.musicrecognizer.glue.library.mapper.TrackFilterToDataMapper
import com.mrsep.musicrecognizer.glue.library.mapper.TrackToDomainMapper
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface MapperModule {

    @Binds
    fun bindTrackToDomainMapper(implementation: TrackToDomainMapper):
            Mapper<TrackEntity, Track>

    @Binds
    fun bindRemoteTrackResultToDomainMapper(implementation: SearchResultToDomainMapper):
            Mapper<SearchDataResult<Track>, SearchResult<Track>>

    @Binds
    fun bindTrackFilterToDataMapper(implementation: TrackFilterToDataMapper):
            Mapper<TrackFilter, TrackDataFilter>

}