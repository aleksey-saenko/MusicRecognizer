package com.mrsep.musicrecognizer.domain

import com.mrsep.musicrecognizer.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface TrackRepository {

    suspend fun insertOrReplace(vararg track: Track)

    suspend fun delete(vararg track: Track)
    suspend fun deleteAll()
    suspend fun deleteAllExceptFavorites()
    suspend fun deleteAllFavorites()

    suspend fun getByMbId(mbId: String): Track?
    suspend fun update(track: Track)
    fun getByMbIdFlow(mbId: String): Flow<Track?>
    suspend fun getAfterDate(date: Long, limit: Int): List<Track>
    suspend fun getLastRecognized(limit: Int): List<Track>
    fun getLastRecognizedFlow(limit: Int): Flow<List<Track>>
    fun getFavoritesFlow(limit: Int): Flow<List<Track>>

    suspend fun search(keyword: String, limit: Int): List<Track>
    fun searchFlow(keyword: String, limit: Int): Flow<List<Track>>

    fun searchResultFlow(keyword: String, limit: Int): Flow<SearchResult<Track>>

}

sealed class SearchResult<T> {
    abstract val keyword: String

    data class Processing<T>(
        override val keyword: String
    ) : SearchResult<T>()

    data class Success<T>(
        override val keyword: String,
        val data: List<T>
    ) : SearchResult<T>() {
        val isEmpty get() = data.isEmpty()
    }

}