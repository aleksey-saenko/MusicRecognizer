package com.mrsep.musicrecognizer.domain

import com.mrsep.musicrecognizer.domain.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.domain.model.Track
import com.mrsep.musicrecognizer.domain.model.UserPreferences
import java.io.File
import java.net.URL

interface RecognitionService {

    suspend fun recognize(
        token: String,
        requiredServices: UserPreferences.RequiredServices,
        file: File
    ): RemoteRecognitionResult<Track>

    suspend fun recognize(
        token: String,
        requiredServices: UserPreferences.RequiredServices,
        byteArray: ByteArray
    ): RemoteRecognitionResult<Track>

    suspend fun recognize(
        token: String,
        requiredServices: UserPreferences.RequiredServices,
        url: URL
    ): RemoteRecognitionResult<Track>

    suspend fun fakeRecognize(): RemoteRecognitionResult<Track>

}

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