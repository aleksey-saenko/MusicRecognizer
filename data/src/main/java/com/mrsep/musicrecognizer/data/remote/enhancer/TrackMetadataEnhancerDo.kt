package com.mrsep.musicrecognizer.data.remote.enhancer

import com.mrsep.musicrecognizer.data.track.TrackEntity

interface TrackMetadataEnhancerDo {

    suspend fun enhance(track: TrackEntity): RemoteMetadataEnhancingResultDo

}