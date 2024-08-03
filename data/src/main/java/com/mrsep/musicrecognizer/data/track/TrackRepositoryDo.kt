package com.mrsep.musicrecognizer.data.track

import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import kotlinx.coroutines.flow.Flow

interface TrackRepositoryDo {

    suspend fun upsert(tracks: List<TrackEntity>)

    suspend fun upsertKeepProperties(tracks: List<TrackEntity>): List<TrackEntity>

    suspend fun updateKeepProperties(tracks: List<TrackEntity>)

    suspend fun update(tracks: List<TrackEntity>)

    suspend fun setThemeSeedColor(trackId: String, color: Int?)

    suspend fun setFavorite(trackId: String, isFavorite: Boolean)

    suspend fun setViewed(trackId: String, isViewed: Boolean)

    suspend fun delete(trackIds: List<String>)

    suspend fun deleteAll()

    fun isEmptyFlow(): Flow<Boolean>

    fun getUnviewedCountFlow(): Flow<Int>

    fun getTrackFlow(trackId: String): Flow<TrackEntity?>

    fun getTracksByFilterFlow(filter: UserPreferencesDo.TrackFilterDo): Flow<List<TrackEntity>>

    fun getSearchResultFlow(query: String, searchScope: Set<TrackDataFieldDo>): Flow<SearchResultDo>
}
