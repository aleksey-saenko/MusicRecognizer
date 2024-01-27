package com.mrsep.musicrecognizer.data.remote.artwork

import com.mrsep.musicrecognizer.data.track.TrackEntity

interface ArtworkFetcher {

    suspend fun fetchUrl(track: TrackEntity): String?

}