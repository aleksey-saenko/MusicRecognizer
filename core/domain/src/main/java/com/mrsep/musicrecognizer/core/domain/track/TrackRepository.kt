package com.mrsep.musicrecognizer.core.domain.track

import com.mrsep.musicrecognizer.core.domain.preferences.TrackFilter
import com.mrsep.musicrecognizer.core.domain.track.model.SearchResult
import com.mrsep.musicrecognizer.core.domain.track.model.Track
import com.mrsep.musicrecognizer.core.domain.track.model.TrackDataField
import com.mrsep.musicrecognizer.core.domain.track.model.TrackPreview
import kotlinx.coroutines.flow.Flow

interface TrackRepository {

    suspend fun upsert(tracks: List<Track>)

    suspend fun upsertKeepProperties(tracks: List<Track>): List<Track>

    suspend fun updateKeepProperties(tracks: List<Track>)

    suspend fun update(tracks: List<Track>)

    suspend fun setThemeSeedColor(trackId: String, color: Int?)

    suspend fun setFavorite(trackId: String, isFavorite: Boolean)

    suspend fun setViewed(trackId: String, isViewed: Boolean)

    suspend fun delete(trackIds: List<String>)

    suspend fun deleteAll()

    fun isEmptyFlow(): Flow<Boolean>

    fun getUnviewedCountFlow(): Flow<Int>

    fun getTrackFlow(trackId: String): Flow<Track?>

    fun getPreviewsByFilterFlow(filter: TrackFilter): Flow<List<TrackPreview>>

    fun getSearchResultFlow(query: String, searchScope: Set<TrackDataField>): Flow<SearchResult>
}
