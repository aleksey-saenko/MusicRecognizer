package com.mrsep.musicrecognizer.feature.recognition.presentation.model

import androidx.compose.runtime.Immutable
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RemoteRecognitionResult

@Immutable
internal sealed class RemoteRecognitionResultUi {

    data class Success(val track: TrackUi) : RemoteRecognitionResultUi()

    data object NoMatches : RemoteRecognitionResultUi()

    sealed class Error : RemoteRecognitionResultUi() {

        data object BadConnection : Error()

        data class BadRecording(
            val message: String = "",
            val eStackTrace: String? = null
        ) : Error()

        data object AuthError : Error()

        data object ApiUsageLimited : Error()

        data class HttpError(
            val code: Int,
            val message: String
        ) : Error()

        data class UnhandledError(
            val message: String = "",
            val eStackTrace: String? = null
        ) : Error()

    }

}

internal fun RemoteRecognitionResult.toUi() = when (this) {
    is RemoteRecognitionResult.Success -> RemoteRecognitionResultUi.Success(track.toUi())

    RemoteRecognitionResult.NoMatches -> RemoteRecognitionResultUi.NoMatches

    RemoteRecognitionResult.Error.BadConnection -> RemoteRecognitionResultUi.Error.BadConnection

    is RemoteRecognitionResult.Error.BadRecording -> RemoteRecognitionResultUi.Error.BadRecording(
        message = message,
        eStackTrace = cause?.stackTraceToString()
    )

    RemoteRecognitionResult.Error.AuthError -> RemoteRecognitionResultUi.Error.AuthError

    RemoteRecognitionResult.Error.ApiUsageLimited -> RemoteRecognitionResultUi.Error.ApiUsageLimited

    is RemoteRecognitionResult.Error.HttpError -> RemoteRecognitionResultUi.Error.HttpError(
        code = code,
        message = message
    )

    is RemoteRecognitionResult.Error.UnhandledError -> RemoteRecognitionResultUi.Error.UnhandledError(
        message = message,
        eStackTrace = cause?.stackTraceToString()
    )
}