package com.mrsep.musicrecognizer.domain

import com.mrsep.musicrecognizer.domain.model.RemoteRecognizeResult
import com.mrsep.musicrecognizer.domain.model.Track
import java.io.File

interface RecognizeService {

    suspend fun recognize(file: File): RemoteRecognizeResult<Track>
    suspend fun fakeRecognize(): RemoteRecognizeResult<Track>

}

sealed interface RecognizeStatus {

    object Ready : RecognizeStatus
    object Listening : RecognizeStatus
    object Recognizing : RecognizeStatus
    data class Success(val track: Track) : RecognizeStatus
    object NoMatches: RecognizeStatus

    sealed interface Error : RecognizeStatus {

        data class RecordError(val error: RecordResult.Error): Error
        data class RemoteError(val error: RemoteRecognizeResult.Error): Error

    }



}