package com.mrsep.musicrecognizer.domain

import com.mrsep.musicrecognizer.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface TrackRepository {

    suspend fun insertOrReplace(vararg track: Track)

    suspend fun getByMbId(mbId: String): Track?

    suspend fun update(track: Track)

    fun getByMbIdFlow(mbId: String): Flow<Track?>

    suspend fun getAfterDate(date: Long, limit: Int): List<Track>

    suspend fun getLastRecognized(limit: Int): List<Track>

    fun getLastRecognizedFlow(limit: Int): Flow<List<Track>>

    fun getFavoritesFlow(limit: Int): Flow<List<Track>>


}