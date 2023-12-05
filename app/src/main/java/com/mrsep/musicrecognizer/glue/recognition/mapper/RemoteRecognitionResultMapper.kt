package com.mrsep.musicrecognizer.glue.recognition.mapper

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionResultDo
import com.mrsep.musicrecognizer.data.track.TrackEntity
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.Track
import javax.inject.Inject

class RemoteRecognitionResultMapper @Inject constructor(
    private val trackMapper: BidirectionalMapper<TrackEntity, Track>
) : Mapper<RemoteRecognitionResultDo, RemoteRecognitionResult> {

    override fun map(input: RemoteRecognitionResultDo): RemoteRecognitionResult {
        return when (input) {
            RemoteRecognitionResultDo.NoMatches ->
                RemoteRecognitionResult.NoMatches

            is RemoteRecognitionResultDo.Success ->
                RemoteRecognitionResult.Success(trackMapper.map(input.data))

            RemoteRecognitionResultDo.Error.BadConnection ->
                RemoteRecognitionResult.Error.BadConnection

            is RemoteRecognitionResultDo.Error.BadRecording ->
                RemoteRecognitionResult.Error.BadRecording(input.message)

            is RemoteRecognitionResultDo.Error.HttpError ->
                RemoteRecognitionResult.Error.HttpError(input.code, input.message)

            is RemoteRecognitionResultDo.Error.UnhandledError ->
                RemoteRecognitionResult.Error.UnhandledError(input.message, input.e)

            is RemoteRecognitionResultDo.Error.WrongToken ->
                RemoteRecognitionResult.Error.WrongToken(input.isLimitReached)
        }
    }
}