package com.mrsep.musicrecognizer.domain

import com.mrsep.musicrecognizer.domain.model.RecognizeResult
import com.mrsep.musicrecognizer.domain.model.Track
import java.io.File

interface RecognizeService {
    suspend fun recognize(file: File): RecognizeResult<Track>
    suspend fun fakeRecognize(): RecognizeResult<Track>
}

sealed interface RecognizeStatus {

    object Ready : RecognizeStatus
    object Listening : RecognizeStatus
    object Recognizing : RecognizeStatus
    data class Success(val result: RecognizeResult<Track>) : RecognizeStatus
    object Failure : RecognizeStatus

}