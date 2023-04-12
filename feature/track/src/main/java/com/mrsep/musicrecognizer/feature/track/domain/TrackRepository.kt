package com.mrsep.musicrecognizer.feature.track.domain

import com.mrsep.musicrecognizer.feature.track.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface TrackRepository {

    suspend fun update(track: Track)
    fun getByMbIdFlow(mbId: String): Flow<Track?>

}