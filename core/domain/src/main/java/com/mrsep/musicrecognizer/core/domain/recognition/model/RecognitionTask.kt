package com.mrsep.musicrecognizer.core.domain.recognition.model

sealed class RecognitionTask {

    data class Created(
        val id: Int,
        val launched: Boolean
    ) : RecognitionTask()

    // corresponds "do not create enqueued recognition after bad recognition result"
    data object Ignored : RecognitionTask()

    data class Error(val cause: Throwable? = null) : RecognitionTask()
}
