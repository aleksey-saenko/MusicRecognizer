package com.mrsep.musicrecognizer.core.domain.recognition

import kotlinx.coroutines.flow.Flow

interface TrackMetadataEnhancerScheduler {

    fun enqueueTrackLinksFetcher(trackId: String)
    fun enqueueLyricsFetcher(trackId: String)

    fun isTrackLinksFetcherRunning(trackId: String): Flow<Boolean>
    fun isLyricsFetcherRunning(trackId: String): Flow<Boolean>

    fun cancelTrackLinksFetcher(trackId: String)
    fun cancelLyricsFetcher(trackId: String)

    fun cancelAll()
}
