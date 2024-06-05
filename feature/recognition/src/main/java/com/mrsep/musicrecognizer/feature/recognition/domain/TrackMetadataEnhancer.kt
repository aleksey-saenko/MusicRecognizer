package com.mrsep.musicrecognizer.feature.recognition.domain

import com.mrsep.musicrecognizer.feature.recognition.domain.model.RemoteMetadataEnhancingResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.Track

interface TrackMetadataEnhancer {

    suspend fun enhance(track: Track): RemoteMetadataEnhancingResult
}
