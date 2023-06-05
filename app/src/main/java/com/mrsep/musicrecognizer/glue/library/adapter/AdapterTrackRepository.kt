package com.mrsep.musicrecognizer.glue.library.adapter

import androidx.paging.PagingData
import androidx.paging.map
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.track.SearchDataResult
import com.mrsep.musicrecognizer.data.track.TrackDataFilter
import com.mrsep.musicrecognizer.data.track.TrackDataRepository
import com.mrsep.musicrecognizer.data.track.TrackEntity
import com.mrsep.musicrecognizer.feature.library.domain.model.SearchResult
import com.mrsep.musicrecognizer.feature.library.domain.model.Track
import com.mrsep.musicrecognizer.feature.library.domain.model.TrackFilter
import com.mrsep.musicrecognizer.feature.library.domain.repository.TrackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AdapterTrackRepository @Inject constructor(
    private val trackDataRepository: TrackDataRepository,
    private val trackMapper: Mapper<TrackEntity, Track>,
    private val searchResultMapper: Mapper<SearchDataResult<Track>, SearchResult<Track>>,
    private val trackFilterMapper: Mapper<TrackFilter, TrackDataFilter>
) : TrackRepository {

    override fun isEmptyFlow(): Flow<Boolean> {
        return trackDataRepository.isEmptyFlow()
    }

    override fun getPagedFlow(): Flow<PagingData<Track>> {
        return trackDataRepository.getPagedFlow()
            .map { paging -> paging.map { entity -> trackMapper.map(entity) } }
    }

    override fun getFilteredFlow(filter: TrackFilter): Flow<List<Track>> {
        return trackDataRepository.getFilteredFlow(trackFilterMapper.map(filter))
            .map { list -> list.map { entity -> trackMapper.map(entity) } }
    }

    override suspend fun search(keyword: String, limit: Int): List<Track> {
        return trackDataRepository.search(keyword, limit)
            .map { entity -> trackMapper.map(entity) }
    }

    override fun searchResultFlow(keyword: String, limit: Int): Flow<SearchResult<Track>> {
        return trackDataRepository.searchResultFlow(keyword, limit).map { searchResult ->
            searchResultMapper.map(
                searchResult.map { entity -> trackMapper.map(entity) }
            )
        }
    }

}