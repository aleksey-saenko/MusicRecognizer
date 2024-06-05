package com.mrsep.musicrecognizer.glue.track.adapter

import com.mrsep.musicrecognizer.feature.track.domain.TrackMetadataEnhancerScheduler
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import com.mrsep.musicrecognizer.feature.recognition.domain.TrackMetadataEnhancerScheduler as OuterMetadataEnhancerScheduler

class AdapterTrackMetadataEnhancerScheduler @Inject constructor(
    private val outerMetadataEnhancerScheduler: OuterMetadataEnhancerScheduler
) : TrackMetadataEnhancerScheduler {

    override fun isRunning(trackId: String): Flow<Boolean> {
        return outerMetadataEnhancerScheduler.isRunning(trackId)
    }
}
