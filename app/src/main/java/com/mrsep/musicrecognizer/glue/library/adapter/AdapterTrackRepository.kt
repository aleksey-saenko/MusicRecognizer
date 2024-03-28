package com.mrsep.musicrecognizer.glue.library.adapter

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import com.mrsep.musicrecognizer.data.track.SearchResultDo
import com.mrsep.musicrecognizer.data.track.TrackDataFieldDo
import com.mrsep.musicrecognizer.data.track.TrackRepositoryDo
import com.mrsep.musicrecognizer.data.track.TrackEntity
import com.mrsep.musicrecognizer.feature.library.domain.model.SearchResult
import com.mrsep.musicrecognizer.feature.library.domain.model.Track
import com.mrsep.musicrecognizer.feature.library.domain.model.TrackDataField
import com.mrsep.musicrecognizer.feature.library.domain.model.TrackFilter
import com.mrsep.musicrecognizer.feature.library.domain.repository.TrackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AdapterTrackRepository @Inject constructor(
    private val trackRepositoryDo: TrackRepositoryDo,
    private val trackMapper: Mapper<TrackEntity, Track>,
    private val searchResultMapper: Mapper<SearchResultDo, SearchResult>,
    private val trackFilterMapper: BidirectionalMapper<UserPreferencesDo.TrackFilterDo, TrackFilter>,
    private val trackDataFieldMapper: BidirectionalMapper<TrackDataFieldDo, TrackDataField>,
) : TrackRepository {

    override fun isEmptyFlow(): Flow<Boolean> {
        return trackRepositoryDo.isEmptyFlow()
    }

    override fun getTracksByFilterFlow(filter: TrackFilter): Flow<List<Track>> {
        return trackRepositoryDo.getTracksByFilterFlow(trackFilterMapper.reverseMap(filter))
            .map { trackList -> trackList.map(trackMapper::map) }
    }

    override fun getSearchResultFlow(
        query: String,
        searchScope: Set<TrackDataField>
    ): Flow<SearchResult> {
        val searchScopeDo = searchScope.map(trackDataFieldMapper::reverseMap).toSet()
        return trackRepositoryDo.getSearchResultFlow(query, searchScopeDo)
            .map(searchResultMapper::map)
    }

    override suspend fun delete(vararg trackIds: String) {
        trackRepositoryDo.delete(*trackIds)
    }

}