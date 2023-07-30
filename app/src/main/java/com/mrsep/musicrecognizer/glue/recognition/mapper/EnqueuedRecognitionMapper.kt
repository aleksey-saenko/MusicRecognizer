package com.mrsep.musicrecognizer.glue.recognition.mapper

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionEntity
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionEntityWithTrack
import com.mrsep.musicrecognizer.data.enqueued.model.RemoteRecognitionResultType
import com.mrsep.musicrecognizer.data.track.TrackEntity
import com.mrsep.musicrecognizer.feature.recognition.domain.model.EnqueuedRecognition
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.Track
import javax.inject.Inject

class EnqueuedRecognitionMapper @Inject constructor(
    private val trackMapper: BidirectionalMapper<TrackEntity, Track>
) : BidirectionalMapper<EnqueuedRecognitionEntityWithTrack, EnqueuedRecognition> {

    override fun map(input: EnqueuedRecognitionEntityWithTrack): EnqueuedRecognition {
        return EnqueuedRecognition(
            id = input.enqueued.id,
            title = input.enqueued.title,
            recordFile = input.enqueued.recordFile,
            creationDate = input.enqueued.creationDate,
            result = input.track?.let { trackEntity ->
                RemoteRecognitionResult.Success(trackMapper.map(trackEntity))
            } ?: run {
                when (input.enqueued.resultType) {
                    RemoteRecognitionResultType.Success -> null
                    RemoteRecognitionResultType.NoMatches -> RemoteRecognitionResult.NoMatches
                    RemoteRecognitionResultType.BadConnection -> RemoteRecognitionResult.Error.BadConnection
                    RemoteRecognitionResultType.BadRecording ->
                        RemoteRecognitionResult.Error.BadRecording(
                            message = input.enqueued.resultMessage ?: ""
                        )

                    RemoteRecognitionResultType.WrongToken ->
                        RemoteRecognitionResult.Error.WrongToken(false)

                    RemoteRecognitionResultType.LimitedToken ->
                        RemoteRecognitionResult.Error.WrongToken(true)

                    RemoteRecognitionResultType.HttpError -> {
                        val combMessage = input.enqueued.resultMessage ?: ""
                        val code = combMessage.substringBefore(
                            ":", "-1"
                        ).toIntOrNull() ?: -1
                        val message = combMessage.substringAfter(
                            ":", "Unexpected error"
                        )
                        RemoteRecognitionResult.Error.HttpError(code, message)
                    }

                    RemoteRecognitionResultType.UnhandledError ->
                        RemoteRecognitionResult.Error.UnhandledError(
                            message = input.enqueued.resultMessage ?: ""
                        )

                    null -> null
                }
            },
            resultDate = input.enqueued.resultDate
        )
    }

    override fun reverseMap(input: EnqueuedRecognition): EnqueuedRecognitionEntityWithTrack {
        var resultType: RemoteRecognitionResultType? = null
        var resultMessage: String? = null
        var trackMbId: String? = null
        when (val result = input.result) {
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
            is RemoteRecognitionResult.Error.WrongToken -> {
                resultType =  if (result.isLimitReached) {
                    RemoteRecognitionResultType.LimitedToken
                } else {
                    RemoteRecognitionResultType.WrongToken
                }
            }
            RemoteRecognitionResult.NoMatches -> {
                resultType = RemoteRecognitionResultType.NoMatches
            }
            is RemoteRecognitionResult.Success -> {
                resultType = RemoteRecognitionResultType.Success
                trackMbId = result.track.mbId
            }
            null -> { /* NO-OP */ }
        }
        val optionalTrack = (input.result as? RemoteRecognitionResult.Success)?.track?.run(
            trackMapper::reverseMap
        )
        return EnqueuedRecognitionEntityWithTrack(
            enqueued = EnqueuedRecognitionEntity(
                id = input.id,
                title = input.title,
                recordFile = input.recordFile,
                creationDate = input.creationDate,
                resultType = resultType,
                resultMbId = trackMbId,
                resultMessage = resultMessage,
                resultDate = input.resultDate
            ),
            track = optionalTrack
        )
    }
}