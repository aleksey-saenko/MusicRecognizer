package com.mrsep.musicrecognizer.feature.track.domain

import com.mrsep.musicrecognizer.feature.track.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface TrackRepository {

    suspend fun toggleFavoriteMark(mbId: String)

    suspend fun deleteByMbId(mbId: String)

    fun getByMbIdFlow(mbId: String): Flow<Track?>

    fun getLyricsFlowById(mbId: String): Flow<String?>

}