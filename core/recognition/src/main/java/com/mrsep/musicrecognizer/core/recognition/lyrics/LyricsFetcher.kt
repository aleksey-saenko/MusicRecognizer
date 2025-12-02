package com.mrsep.musicrecognizer.core.recognition.lyrics

import com.mrsep.musicrecognizer.core.domain.recognition.model.NetworkResult
import com.mrsep.musicrecognizer.core.domain.track.model.Lyrics
import com.mrsep.musicrecognizer.core.domain.track.model.Track

interface LyricsFetcher {

    suspend fun fetch(track: Track): NetworkResult<Lyrics?>
}
