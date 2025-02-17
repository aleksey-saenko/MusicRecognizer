package com.mrsep.musicrecognizer.core.recognition.artwork

import com.mrsep.musicrecognizer.core.domain.track.model.Track

internal interface ArtworkFetcher {

    suspend fun fetchUrl(track: Track): TrackArtwork?
}
