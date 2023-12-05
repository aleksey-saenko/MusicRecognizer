package com.mrsep.musicrecognizer.feature.track.domain

import kotlinx.coroutines.flow.Flow

interface TrackMetadataEnhancerScheduler {

    fun cancel(trackId: String)

    fun isRunning(trackId: String): Flow<Boolean>

}