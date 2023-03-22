package com.mrsep.musicrecognizer.domain

import com.mrsep.musicrecognizer.domain.model.RemoteRecognizeResult
import com.mrsep.musicrecognizer.domain.model.Track
import com.mrsep.musicrecognizer.domain.model.UserPreferences
import java.io.File
import java.net.URL

interface RecognizeService {

    suspend fun recognize(
        token: String,
        requiredServices: UserPreferences.RequiredServices,
        file: File
    ): RemoteRecognizeResult<Track>

    suspend fun recognize(
        token: String,
        requiredServices: UserPreferences.RequiredServices,
        byteArray: ByteArray
    ): RemoteRecognizeResult<Track>

    suspend fun recognize(
        token: String,
        requiredServices: UserPreferences.RequiredServices,
        url: URL
    ): RemoteRecognizeResult<Track>

    suspend fun fakeRecognize(): RemoteRecognizeResult<Track>

}

sealed class RecognizeStatus {
    abstract val isFinalState: Boolean
    abstract val isInProcessState: Boolean

    object Ready : RecognizeStatus() {
        override val isFinalState get() = false
        override val isInProcessState get() = false
    }

    object Listening : RecognizeStatus() {
        override val isFinalState get() = false
        override val isInProcessState get() = true
    }

    object Recognizing : RecognizeStatus() {
        override val isFinalState get() = false
        override val isInProcessState get() = true
    }

    data class Success(val track: Track) : RecognizeStatus() {
        override val isFinalState get() = true
        override val isInProcessState get() = false
    }

    object NoMatches : RecognizeStatus() {
        override val isFinalState get() = true
        override val isInProcessState get() = false
    }

    sealed class Error : RecognizeStatus() {
        override val isFinalState get() = true
        override val isInProcessState get() = false

        data class RecordError(val error: RecordResult.Error) : Error()
        data class RemoteError(val error: RemoteRecognizeResult.Error) : Error()

    }


}