package com.mrsep.musicrecognizer.feature.recognition.domain

import com.mrsep.musicrecognizer.feature.recognition.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface TrackRepository {

    fun getTrackFlow(trackId: String): Flow<Track?>

    suspend fun upsertKeepProperties(vararg tracks: Track): List<Track>

    suspend fun updateKeepProperties(vararg tracks: Track)

    suspend fun setViewed(trackId: String, isViewed: Boolean)
}
