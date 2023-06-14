package com.mrsep.musicrecognizer.data.track

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

interface TrackRepositoryDo {

    fun isEmptyFlow(): Flow<Boolean>
    fun getPagedFlow(): Flow<PagingData<TrackEntity>>
    suspend fun getWithOffset(pageIndex: Int, pageSize: Int): List<TrackEntity>

    suspend fun insertOrReplace(vararg track: TrackEntity)
    suspend fun insertOrReplaceSaveMetadata(vararg track: TrackEntity): List<TrackEntity>
    suspend fun update(track: TrackEntity)
    suspend fun delete(vararg track: TrackEntity)
    suspend fun deleteByMbId(vararg mbId: String)
    suspend fun deleteAll()
    suspend fun deleteAllExceptFavorites()
    suspend fun deleteAllFavorites()
    suspend fun toggleFavoriteMark(mbId: String)

    fun countAllFlow(): Flow<Int>
    fun countFavoritesFlow(): Flow<Int>

    suspend fun getByMbId(mbId: String): TrackEntity?
    fun getByMbIdFlow(mbId: String): Flow<TrackEntity?>
    fun getLyricsFlowById(mbId: String): Flow<String?>

    fun getFilteredFlow(filter: TrackFilterDo): Flow<List<TrackEntity>>
    suspend fun getAfterDate(date: Long, limit: Int): List<TrackEntity>
    suspend fun getLastRecognized(limit: Int): List<TrackEntity>
    fun getLastRecognizedFlow(limit: Int): Flow<List<TrackEntity>>
    fun getNotFavoriteRecentsFlow(limit: Int): Flow<List<TrackEntity>>
    fun getFavoritesFlow(limit: Int): Flow<List<TrackEntity>>

    suspend fun search(keyword: String, limit: Int): List<TrackEntity>
    fun searchFlow(keyword: String, limit: Int): Flow<List<TrackEntity>>
    fun searchResultFlow(keyword: String, limit: Int): Flow<SearchResultDo>
    fun createSearchKeyForSQLite(word: String): String

}