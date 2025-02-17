package com.mrsep.musicrecognizer.core.domain.recognition

import kotlinx.coroutines.flow.Flow

interface TrackMetadataEnhancerScheduler {

    fun enqueue(trackId: String)

    fun isRunning(trackId: String): Flow<Boolean>

    fun cancel(trackId: String)

    fun cancelAll()
}
