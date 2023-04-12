package com.mrsep.musicrecognizer.glue.recognition.mapper

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionDataResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.Track
import javax.inject.Inject

class RemoteTrackResultToDomainMapper @Inject constructor() :
    Mapper<RemoteRecognitionDataResult<@JvmSuppressWildcards Track>, RemoteRecognitionResult<@JvmSuppressWildcards Track>> {

    override fun map(input: RemoteRecognitionDataResult<Track>): RemoteRecognitionResult<Track> {
        return when (input) {
            RemoteRecognitionDataResult.NoMatches -> RemoteRecognitionResult.NoMatches
            is RemoteRecognitionDataResult.Success -> RemoteRecognitionResult.Success(input.data)
            RemoteRecognitionDataResult.Error.BadConnection -> RemoteRecognitionResult.Error.BadConnection
            is RemoteRecognitionDataResult.Error.HttpError -> RemoteRecognitionResult.Error.HttpError(input.code, input.message)
            is RemoteRecognitionDataResult.Error.UnhandledError -> RemoteRecognitionResult.Error.UnhandledError(input.message, input.e)
            is RemoteRecognitionDataResult.Error.WrongToken -> RemoteRecognitionResult.Error.WrongToken(input.isLimitReached)
        }
    }
}