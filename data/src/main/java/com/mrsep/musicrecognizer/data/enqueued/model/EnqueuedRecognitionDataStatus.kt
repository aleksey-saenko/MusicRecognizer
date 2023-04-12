package com.mrsep.musicrecognizer.data.enqueued.model

sealed class EnqueuedRecognitionDataStatus {

    object Inactive : EnqueuedRecognitionDataStatus()
    object Enqueued : EnqueuedRecognitionDataStatus()
    object Running : EnqueuedRecognitionDataStatus()
    object Canceled : EnqueuedRecognitionDataStatus()

    sealed class Finished : EnqueuedRecognitionDataStatus() {
        object NotFound : Finished()
        data class Success(val trackMbId: String) : Finished()
        data class Error(val message: String) : Finished()
    }

}