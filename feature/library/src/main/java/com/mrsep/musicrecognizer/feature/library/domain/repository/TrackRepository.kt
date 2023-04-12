package com.mrsep.musicrecognizer.feature.library.domain.repository

import androidx.paging.PagingData
import com.mrsep.musicrecognizer.feature.library.domain.model.SearchResult
import com.mrsep.musicrecognizer.feature.library.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface TrackRepository {

    fun getPagedFlow(): Flow<PagingData<Track>>

    fun getLastRecognizedFlow(limit: Int): Flow<List<Track>>
    fun getFavoritesFlow(limit: Int): Flow<List<Track>>

    suspend fun search(keyword: String, limit: Int): List<Track>

    fun searchResultFlow(keyword: String, limit: Int): Flow<SearchResult<Track>>

}