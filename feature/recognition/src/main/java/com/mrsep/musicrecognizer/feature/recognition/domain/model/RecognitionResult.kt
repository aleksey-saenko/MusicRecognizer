package com.mrsep.musicrecognizer.feature.recognition.domain.model

sealed class RecognitionResult {

    data class Success(val track: Track) : RecognitionResult()

    data class NoMatches(val recognitionTask: RecognitionTask) : RecognitionResult()

    data class ScheduledOffline(val recognitionTask: RecognitionTask) : RecognitionResult()

    data class Error(
        val remoteError: RemoteRecognitionResult.Error,
        val recognitionTask: RecognitionTask
    ) : RecognitionResult()
}
