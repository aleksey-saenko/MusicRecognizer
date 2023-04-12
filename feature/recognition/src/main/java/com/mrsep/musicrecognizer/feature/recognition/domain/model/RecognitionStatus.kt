package com.mrsep.musicrecognizer.feature.recognition.domain.model

import com.mrsep.musicrecognizer.feature.recognition.domain.RecordResult
import java.io.File

sealed class RecognitionStatus {
    abstract val isFinalState: Boolean
    abstract val isInProcessState: Boolean

    object Ready : RecognitionStatus() {
        override val isFinalState get() = false
        override val isInProcessState get() = false
    }

    object Listening : RecognitionStatus() {
        override val isFinalState get() = false
        override val isInProcessState get() = true
    }

    object Recognizing : RecognitionStatus() {
        override val isFinalState get() = false
        override val isInProcessState get() = true
    }

    data class Success(val track: Track) : RecognitionStatus() {
        override val isFinalState get() = true
        override val isInProcessState get() = false
    }

    data class NoMatches(val record: File) : RecognitionStatus() {
        override val isFinalState get() = true
        override val isInProcessState get() = false
    }

    sealed class Error : RecognitionStatus() {
        override val isFinalState get() = true
        override val isInProcessState get() = false

        data class RecordError(val error: RecordResult.Error) : Error()
        data class RemoteError(
            val error: RemoteRecognitionResult.Error,
            val record: File
        ) : Error()
    }

}