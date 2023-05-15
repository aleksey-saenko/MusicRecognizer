package com.mrsep.musicrecognizer.feature.recognition.domain.model

sealed class RecognitionTask {

    data class Created(
        val id: Int,
        val launched: Boolean
    ): RecognitionTask() {
        override fun getEnqueuedIdOrNull() = id
    }

    object Ignored: RecognitionTask() {
        override fun getEnqueuedIdOrNull() = null
    }

    object Error: RecognitionTask() {
        override fun getEnqueuedIdOrNull() = null
    }

    abstract fun getEnqueuedIdOrNull(): Int?

}