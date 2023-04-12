package com.mrsep.musicrecognizer.data.track

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

interface TrackDataRepository {
    fun getPagedFlow(): Flow<PagingData<TrackEntity>>

    suspend fun getWithOffset(pageIndex: Int, pageSize: Int): List<TrackEntity>

    suspend fun insertOrReplace(vararg track: TrackEntity)

    suspend fun insertOrReplaceSaveMetadata(vararg track: TrackEntity): List<TrackEntity>

    suspend fun update(track: TrackEntity)

    suspend fun delete(vararg track: TrackEntity)

    suspend fun deleteAll()

    suspend fun deleteAllExceptFavorites()

    suspend fun deleteAllFavorites()

    suspend fun getByMbId(mbId: String): TrackEntity?
    fun getByMbIdFlow(mbId: String): Flow<TrackEntity?>

    suspend fun getAfterDate(date: Long, limit: Int): List<TrackEntity>

    suspend fun getLastRecognized(limit: Int): List<TrackEntity>
    fun getLastRecognizedFlow(limit: Int): Flow<List<TrackEntity>>
    fun getFavoritesFlow(limit: Int): Flow<List<TrackEntity>>

    suspend fun search(keyword: String, limit: Int): List<TrackEntity>
    fun searchFlow(keyword: String, limit: Int): Flow<List<TrackEntity>>
    fun searchResultFlow(keyword: String, limit: Int): Flow<SearchDataResult<TrackEntity>>
    fun createSearchKeyForSQLite(word: String): String
}