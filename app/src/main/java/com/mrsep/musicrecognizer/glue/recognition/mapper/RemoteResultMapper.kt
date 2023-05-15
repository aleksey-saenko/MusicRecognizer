package com.mrsep.musicrecognizer.glue.recognition.mapper

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionDataResult
import com.mrsep.musicrecognizer.data.track.TrackEntity
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.Track
import javax.inject.Inject

class RemoteResultMapper @Inject constructor(
    private val trackMapper: BidirectionalMapper<TrackEntity, Track>
) :
    Mapper<RemoteRecognitionDataResult, RemoteRecognitionResult> {

    override fun map(input: RemoteRecognitionDataResult): RemoteRecognitionResult {
        return when (input) {
            RemoteRecognitionDataResult.NoMatches -> RemoteRecognitionResult.NoMatches
            is RemoteRecognitionDataResult.Success -> RemoteRecognitionResult.Success(trackMapper.map(input.data))
            RemoteRecognitionDataResult.Error.BadConnection -> RemoteRecognitionResult.Error.BadConnection
            is RemoteRecognitionDataResult.Error.BadRecording -> RemoteRecognitionResult.Error.BadRecording(RuntimeException(input.message))
            is RemoteRecognitionDataResult.Error.HttpError -> RemoteRecognitionResult.Error.HttpError(input.code, input.message)
            is RemoteRecognitionDataResult.Error.UnhandledError -> RemoteRecognitionResult.Error.UnhandledError(input.message, input.e)
            is RemoteRecognitionDataResult.Error.WrongToken -> RemoteRecognitionResult.Error.WrongToken(input.isLimitReached)
        }
    }
}