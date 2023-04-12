package com.mrsep.musicrecognizer.feature.recognition.domain

import com.mrsep.musicrecognizer.feature.recognition.domain.model.Track

interface TrackRepository {

    suspend fun insertOrReplace(vararg track: Track)
    suspend fun insertOrReplaceSaveMetadata(vararg track: Track): List<Track>

    suspend fun getByMbId(mbId: String): Track?
    suspend fun update(track: Track)


}