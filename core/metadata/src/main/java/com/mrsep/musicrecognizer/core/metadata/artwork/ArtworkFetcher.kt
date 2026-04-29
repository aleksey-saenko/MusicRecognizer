package com.mrsep.musicrecognizer.core.metadata.artwork

import com.mrsep.musicrecognizer.core.domain.recognition.model.NetworkResult
import com.mrsep.musicrecognizer.core.domain.track.model.Track

interface ArtworkFetcher {

    suspend fun fetchUrl(track: Track): NetworkResult<TrackArtwork?>
}
