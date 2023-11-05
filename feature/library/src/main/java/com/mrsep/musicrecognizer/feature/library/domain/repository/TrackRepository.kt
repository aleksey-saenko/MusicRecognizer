package com.mrsep.musicrecognizer.feature.library.domain.repository

import com.mrsep.musicrecognizer.feature.library.domain.model.SearchResult
import com.mrsep.musicrecognizer.feature.library.domain.model.Track
import com.mrsep.musicrecognizer.feature.library.domain.model.TrackFilter
import kotlinx.coroutines.flow.Flow

interface TrackRepository {

    fun isEmptyFlow(): Flow<Boolean>

    fun getFilteredFlow(filter: TrackFilter): Flow<List<Track>>

    suspend fun search(keyword: String, limit: Int): List<Track>

    fun searchResultFlow(keyword: String, limit: Int): Flow<SearchResult>

    suspend fun deleteByMbId(vararg mbId: String)

}