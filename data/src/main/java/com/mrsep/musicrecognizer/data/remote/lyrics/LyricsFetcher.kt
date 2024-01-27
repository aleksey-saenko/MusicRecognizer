package com.mrsep.musicrecognizer.data.remote.lyrics

import com.mrsep.musicrecognizer.data.track.TrackEntity

interface LyricsFetcher {

    suspend fun fetch(track: TrackEntity): String?

}