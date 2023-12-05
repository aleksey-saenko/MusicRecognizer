package com.mrsep.musicrecognizer.glue.recognition.mapper

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.remote.enhancer.RemoteMetadataEnhancingResultDo
import com.mrsep.musicrecognizer.data.track.TrackEntity
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RemoteMetadataEnhancingResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.Track
import javax.inject.Inject

class RemoteEnhancingResultMapper @Inject constructor(
    private val trackMapper: BidirectionalMapper<TrackEntity, Track>
) : Mapper<RemoteMetadataEnhancingResultDo, RemoteMetadataEnhancingResult> {

    override fun map(input: RemoteMetadataEnhancingResultDo): RemoteMetadataEnhancingResult {
        return when (input) {
            RemoteMetadataEnhancingResultDo.Error.BadConnection ->
                RemoteMetadataEnhancingResult.Error.BadConnection

            is RemoteMetadataEnhancingResultDo.Error.HttpError ->
                RemoteMetadataEnhancingResult.Error.HttpError(input.code, input.message)

            is RemoteMetadataEnhancingResultDo.Error.UnhandledError ->
                RemoteMetadataEnhancingResult.Error.UnhandledError(input.message, input.cause)

            RemoteMetadataEnhancingResultDo.NoEnhancement ->
                RemoteMetadataEnhancingResult.NoEnhancement

            is RemoteMetadataEnhancingResultDo.Success ->
                RemoteMetadataEnhancingResult.Success(trackMapper.map(input.track))
        }
    }

}