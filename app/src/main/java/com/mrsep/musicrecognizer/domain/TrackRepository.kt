package com.mrsep.musicrecognizer.domain

import com.mrsep.musicrecognizer.domain.model.Track

interface TrackRepository {

    suspend fun insertOrReplace(vararg track: Track)

    suspend fun getUnique(mbId: String): Track?

    suspend fun getAfterDate(date: Long, limit: Int): List<Track>

    suspend fun getLast(limit: Int): List<Track>

}