package com.mrsep.musicrecognizer.glue.library.adapter

import androidx.paging.PagingData
import androidx.paging.map
import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import com.mrsep.musicrecognizer.data.track.SearchResultDo
import com.mrsep.musicrecognizer.data.track.TrackRepositoryDo
import com.mrsep.musicrecognizer.data.track.TrackEntity
import com.mrsep.musicrecognizer.feature.library.domain.model.SearchResult
import com.mrsep.musicrecognizer.feature.library.domain.model.Track
import com.mrsep.musicrecognizer.feature.library.domain.model.TrackFilter
import com.mrsep.musicrecognizer.feature.library.domain.repository.TrackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AdapterTrackRepository @Inject constructor(
    private val trackRepositoryDo: TrackRepositoryDo,
    private val trackMapper: Mapper<TrackEntity, Track>,
    private val searchResultMapper: Mapper<SearchResultDo, SearchResult>,
    private val trackFilterMapper: BidirectionalMapper<UserPreferencesDo.TrackFilterDo, TrackFilter>
) : TrackRepository {

    override fun isEmptyFlow(): Flow<Boolean> {
        return trackRepositoryDo.isEmptyFlow()
    }

    override fun getPagedFlow(): Flow<PagingData<Track>> {
        return trackRepositoryDo.getPagedFlow()
            .map { paging -> paging.map { entity -> trackMapper.map(entity) } }
    }

    override fun getFilteredFlow(filter: TrackFilter): Flow<List<Track>> {
        return trackRepositoryDo.getFilteredFlow(trackFilterMapper.reverseMap(filter))
            .map { list -> list.map { entity -> trackMapper.map(entity) } }
    }

    override suspend fun search(keyword: String, limit: Int): List<Track> {
        return trackRepositoryDo.search(keyword, limit)
            .map { entity -> trackMapper.map(entity) }
    }

    override fun searchResultFlow(keyword: String, limit: Int): Flow<SearchResult> {
        return trackRepositoryDo.searchResultFlow(keyword, limit).map(searchResultMapper::map)
    }

}