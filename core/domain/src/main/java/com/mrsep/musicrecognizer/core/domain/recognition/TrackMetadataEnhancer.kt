package com.mrsep.musicrecognizer.core.domain.recognition

import com.mrsep.musicrecognizer.core.domain.recognition.model.RemoteMetadataEnhancingResult
import com.mrsep.musicrecognizer.core.domain.track.model.Track

interface TrackMetadataEnhancer {

    suspend fun enhance(track: Track): RemoteMetadataEnhancingResult
}
