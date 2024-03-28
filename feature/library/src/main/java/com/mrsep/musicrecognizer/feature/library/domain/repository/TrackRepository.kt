package com.mrsep.musicrecognizer.feature.library.domain.repository

import com.mrsep.musicrecognizer.feature.library.domain.model.SearchResult
import com.mrsep.musicrecognizer.feature.library.domain.model.Track
import com.mrsep.musicrecognizer.feature.library.domain.model.TrackDataField
import com.mrsep.musicrecognizer.feature.library.domain.model.TrackFilter
import kotlinx.coroutines.flow.Flow

interface TrackRepository {

    fun isEmptyFlow(): Flow<Boolean>

    fun getTracksByFilterFlow(filter: TrackFilter): Flow<List<Track>>

    fun getSearchResultFlow(query: String, searchScope: Set<TrackDataField>): Flow<SearchResult>

    suspend fun delete(vararg trackIds: String)

}