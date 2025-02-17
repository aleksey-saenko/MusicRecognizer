package com.mrsep.musicrecognizer.core.recognition.lyrics

import com.mrsep.musicrecognizer.core.domain.track.model.Track

internal interface LyricsFetcher {

    suspend fun fetch(track: Track): String?
}
