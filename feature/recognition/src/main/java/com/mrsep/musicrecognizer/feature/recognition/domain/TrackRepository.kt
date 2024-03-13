package com.mrsep.musicrecognizer.feature.recognition.domain

import com.mrsep.musicrecognizer.feature.recognition.domain.model.Track

interface TrackRepository {

    suspend fun upsertKeepProperties(vararg tracks: Track): List<Track>

    suspend fun updateKeepProperties(vararg tracks: Track)

    suspend fun getTrack(trackId: String): Track?

    suspend fun setViewed(trackId: String, isViewed: Boolean)

}