package com.mrsep.musicrecognizer.feature.recognition.domain

import com.mrsep.musicrecognizer.feature.recognition.domain.model.Track
import java.time.Instant

interface TrackRepository {

    suspend fun upsertKeepUserProperties(vararg tracks: Track): List<Track>

    suspend fun updateKeepUserProperties(vararg tracks: Track)

    suspend fun getTrack(trackId: String): Track?

    suspend fun setRecognitionDate(trackId: String, recognitionDate: Instant)

}