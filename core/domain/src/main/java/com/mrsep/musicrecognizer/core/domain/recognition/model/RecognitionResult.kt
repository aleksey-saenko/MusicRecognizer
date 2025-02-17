package com.mrsep.musicrecognizer.core.domain.recognition.model

import com.mrsep.musicrecognizer.core.domain.track.model.Track

sealed class RecognitionResult {

    data class Success(val track: Track) : RecognitionResult()

    data class NoMatches(val recognitionTask: RecognitionTask) : RecognitionResult()

    data class ScheduledOffline(val recognitionTask: RecognitionTask) : RecognitionResult()

    data class Error(
        val remoteError: RemoteRecognitionResult.Error,
        val recognitionTask: RecognitionTask
    ) : RecognitionResult()
}
