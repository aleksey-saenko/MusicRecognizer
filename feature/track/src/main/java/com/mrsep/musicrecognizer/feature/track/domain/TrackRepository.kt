package com.mrsep.musicrecognizer.feature.track.domain

import com.mrsep.musicrecognizer.feature.track.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface TrackRepository {

    suspend fun toggleFavoriteMark(mbId: String)

    suspend fun deleteByMbId(mbId: String)

    suspend fun updateThemeSeedColor(mbId: String, color: Int?)

    fun getByMbIdFlow(mbId: String): Flow<Track?>

}