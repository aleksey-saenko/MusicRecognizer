package com.mrsep.musicrecognizer.feature.recognition.domain.model

sealed class RecognitionResult {

    data class Success(val track: Track) : RecognitionResult()

    class NoMatches(val audioRecording: ByteArray) : RecognitionResult()

    class Error(
        val remoteError: RemoteRecognitionResult.Error,
        val audioRecording: ByteArray
    ) : RecognitionResult()

}