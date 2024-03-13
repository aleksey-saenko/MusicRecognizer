package com.mrsep.musicrecognizer.data.track

import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import kotlinx.coroutines.flow.Flow

interface TrackRepositoryDo {

    suspend fun upsert(vararg tracks: TrackEntity)

    suspend fun upsertKeepProperties(vararg tracks: TrackEntity): List<TrackEntity>

    suspend fun updateKeepProperties(vararg tracks: TrackEntity)

    suspend fun update(vararg tracks: TrackEntity)

    suspend fun setThemeSeedColor(trackId: String, color: Int?)

    suspend fun setFavorite(trackId: String, isFavorite: Boolean)

    suspend fun setViewed(trackId: String, isViewed: Boolean)

    suspend fun delete(vararg trackIds: String)

    suspend fun deleteAll()

    suspend fun getTrack(trackId: String): TrackEntity?

    fun isEmptyFlow(): Flow<Boolean>

    fun getUnviewedCountFlow(): Flow<Int>

    fun getTrackFlow(trackId: String): Flow<TrackEntity?>

    fun getTracksByFilterFlow(filter: UserPreferencesDo.TrackFilterDo): Flow<List<TrackEntity>>

    fun getSearchResultFlow(keyword: String, limit: Int): Flow<SearchResultDo>

}