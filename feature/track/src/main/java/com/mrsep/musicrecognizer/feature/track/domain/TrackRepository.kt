package com.mrsep.musicrecognizer.feature.track.domain

import com.mrsep.musicrecognizer.feature.track.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface TrackRepository {

    fun getTrackFlow(trackId: String): Flow<Track?>

    suspend fun setFavorite(trackId: String, isFavorite: Boolean)

    suspend fun delete(trackId: String)

    suspend fun setThemeSeedColor(trackId: String, color: Int?)

    suspend fun setAsViewed(trackId: String)
}
