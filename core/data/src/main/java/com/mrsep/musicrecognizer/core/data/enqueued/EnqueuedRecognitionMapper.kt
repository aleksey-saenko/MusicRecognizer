package com.mrsep.musicrecognizer.core.data.enqueued

import com.mrsep.musicrecognizer.core.data.track.toDomain
import com.mrsep.musicrecognizer.core.database.enqueued.model.EnqueuedRecognitionEntity
import com.mrsep.musicrecognizer.core.database.enqueued.model.EnqueuedRecognitionEntityWithTrack
import com.mrsep.musicrecognizer.core.database.enqueued.model.RemoteRecognitionResultType
import com.mrsep.musicrecognizer.core.domain.recognition.model.EnqueuedRecognition
import com.mrsep.musicrecognizer.core.domain.recognition.model.RemoteRecognitionResult

internal fun EnqueuedRecognition.toEntity(): EnqueuedRecognitionEntity {
    var resultType: RemoteRecognitionResultType? = null
    var resultMessage: String? = null
    var trackId: String? = null

    when (val result = result) {
        RemoteRecognitionResult.Error.BadConnection -> {
            resultType = RemoteRecognitionResultType.BadConnection
        }

        is RemoteRecognitionResult.Error.BadRecording -> {
            resultType = RemoteRecognitionResultType.BadRecording
            resultMessage = result.message
        }

        is RemoteRecognitionResult.Error.HttpError -> {
            resultType = RemoteRecognitionResultType.HttpError
            resultMessage = "${result.code}:${result.message}"
        }

        is RemoteRecognitionResult.Error.UnhandledError -> {
            resultType = RemoteRecognitionResultType.UnhandledError
            resultMessage = result.message
        }

        is RemoteRecognitionResult.Error.AuthError -> {
            resultType = RemoteRecognitionResultType.AuthError
        }

        is RemoteRecognitionResult.Error.ApiUsageLimited -> {
            resultType = RemoteRecognitionResultType.ApiUsageLimited
        }

        RemoteRecognitionResult.NoMatches -> {
            resultType = RemoteRecognitionResultType.NoMatches
        }

        is RemoteRecognitionResult.Success -> {
            resultType = RemoteRecognitionResultType.Success
            trackId = result.track.id
        }

        null -> { /* NO-OP */ }
    }

    return EnqueuedRecognitionEntity(
        id = id,
        title = title,
        recordFile = recordFile,
        creationDate = creationDate,
        resultType = resultType,
        resultTrackId = trackId,
        resultMessage = resultMessage,
        resultDate = resultDate
    )
}

internal fun EnqueuedRecognitionEntityWithTrack.toDomain(): EnqueuedRecognition {
    val result = when (enqueued.resultType) {
        RemoteRecognitionResultType.Success -> track?.toDomain()
            ?.run(RemoteRecognitionResult::Success)

        RemoteRecognitionResultType.NoMatches -> RemoteRecognitionResult.NoMatches
        RemoteRecognitionResultType.BadConnection -> RemoteRecognitionResult.Error.BadConnection
        RemoteRecognitionResultType.BadRecording -> RemoteRecognitionResult.Error.BadRecording(
            message = enqueued.resultMessage ?: ""
        )

        RemoteRecognitionResultType.AuthError -> RemoteRecognitionResult.Error.AuthError

        RemoteRecognitionResultType.ApiUsageLimited -> RemoteRecognitionResult.Error.ApiUsageLimited

        RemoteRecognitionResultType.HttpError -> {
            val combMessage = enqueued.resultMessage ?: ""
            val code = combMessage.substringBefore(":", "-1").toIntOrNull() ?: -1
            val message = combMessage.substringAfter(":", "Unexpected error")
            RemoteRecognitionResult.Error.HttpError(code, message)
        }

        RemoteRecognitionResultType.UnhandledError -> RemoteRecognitionResult.Error.UnhandledError(
            message = enqueued.resultMessage ?: ""
        )

        null -> null
    }
    return EnqueuedRecognition(
        id = enqueued.id,
        title = enqueued.title,
        recordFile = enqueued.recordFile,
        creationDate = enqueued.creationDate,
        result = result,
        resultDate = enqueued.resultDate
    )
}