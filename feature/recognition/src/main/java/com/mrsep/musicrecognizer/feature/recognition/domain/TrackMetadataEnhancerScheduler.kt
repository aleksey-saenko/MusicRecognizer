package com.mrsep.musicrecognizer.feature.recognition.domain

import kotlinx.coroutines.flow.Flow

interface TrackMetadataEnhancerScheduler {

    fun enqueue(trackId: String)

    fun cancel(trackId: String)

    fun cancelAll()

    fun isRunning(trackId: String): Flow<Boolean>

}