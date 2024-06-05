package com.mrsep.musicrecognizer.glue.recognition.adapters

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.remote.enhancer.RemoteMetadataEnhancingResultDo
import com.mrsep.musicrecognizer.data.remote.enhancer.TrackMetadataEnhancerDo
import com.mrsep.musicrecognizer.data.track.TrackEntity
import com.mrsep.musicrecognizer.feature.recognition.domain.TrackMetadataEnhancer
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RemoteMetadataEnhancingResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.Track
import javax.inject.Inject

class AdapterTrackMetadataEnhancer @Inject constructor(
    private val trackMetadataEnhancerDo: TrackMetadataEnhancerDo,
    private val trackMapper: BidirectionalMapper<TrackEntity, Track>,
    private val resultMapper: Mapper<RemoteMetadataEnhancingResultDo, RemoteMetadataEnhancingResult>
) : TrackMetadataEnhancer {

    override suspend fun enhance(track: Track): RemoteMetadataEnhancingResult {
        val trackEntity = trackMapper.reverseMap(track)
        return trackMetadataEnhancerDo.enhance(trackEntity).run(resultMapper::map)
    }
}
