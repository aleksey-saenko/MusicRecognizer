package com.mrsep.musicrecognizer.glue.library.di

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import com.mrsep.musicrecognizer.data.track.SearchResultDo
import com.mrsep.musicrecognizer.data.track.TrackDataFieldDo
import com.mrsep.musicrecognizer.data.track.TrackEntity
import com.mrsep.musicrecognizer.feature.library.domain.model.SearchResult
import com.mrsep.musicrecognizer.feature.library.domain.model.Track
import com.mrsep.musicrecognizer.feature.library.domain.model.TrackDataField
import com.mrsep.musicrecognizer.feature.library.domain.model.TrackFilter
import com.mrsep.musicrecognizer.feature.library.domain.model.UserPreferences
import com.mrsep.musicrecognizer.glue.library.mapper.SearchResultMapper
import com.mrsep.musicrecognizer.glue.library.mapper.TrackDataFieldMapper
import com.mrsep.musicrecognizer.glue.library.mapper.TrackFilterMapper
import com.mrsep.musicrecognizer.glue.library.mapper.TrackMapper
import com.mrsep.musicrecognizer.glue.library.mapper.UserPreferencesMapper
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface MapperModule {

    @Binds
    fun bindTrackMapper(implementation: TrackMapper):
            Mapper<TrackEntity, Track>

    @Binds
    fun bindRemoteTrackResultMapper(implementation: SearchResultMapper):
            Mapper<SearchResultDo, SearchResult>

    @Binds
    fun bindTrackFilterMapper(implementation: TrackFilterMapper):
            BidirectionalMapper<UserPreferencesDo.TrackFilterDo, TrackFilter>

    @Binds
    fun bindTrackDataFieldMapper(implementation: TrackDataFieldMapper):
            BidirectionalMapper<TrackDataFieldDo, TrackDataField>

    @Binds
    fun bindUserPreferencesMapper(implementation: UserPreferencesMapper):
            Mapper<UserPreferencesDo, UserPreferences>
}
