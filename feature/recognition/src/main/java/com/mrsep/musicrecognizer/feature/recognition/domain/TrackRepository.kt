package com.mrsep.musicrecognizer.feature.recognition.domain

import com.mrsep.musicrecognizer.feature.recognition.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface TrackRepository {

    fun getTrackFlow(trackId: String): Flow<Track?>

    suspend fun upsertKeepProperties(track: Track): Track

    suspend fun updateKeepProperties(track: Track)

    suspend fun setViewed(trackId: String, isViewed: Boolean)
}
