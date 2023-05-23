package com.mrsep.musicrecognizer.feature.recognition.domain.model

sealed class RecognitionTask {

    data class Created(
        val id: Int,
        val launched: Boolean
    ): RecognitionTask() {
        override fun getEnqueuedIdOrNull() = id
    }

    // corresponds "do not schedule after bad recognition result"
    object Ignored: RecognitionTask() {
        override fun getEnqueuedIdOrNull() = null
    }

    data class Error(val cause: Throwable? = null): RecognitionTask() {
        override fun getEnqueuedIdOrNull() = null
    }

    abstract fun getEnqueuedIdOrNull(): Int?

}