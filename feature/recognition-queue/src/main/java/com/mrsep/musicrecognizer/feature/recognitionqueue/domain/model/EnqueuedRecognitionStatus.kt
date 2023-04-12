package com.mrsep.musicrecognizer.feature.recognitionqueue.domain.model

sealed class EnqueuedRecognitionStatus {

    object Inactive : EnqueuedRecognitionStatus()
    object Enqueued : EnqueuedRecognitionStatus()
    object Running : EnqueuedRecognitionStatus()
    object Canceled : EnqueuedRecognitionStatus()

    sealed class Finished : EnqueuedRecognitionStatus() {
        object NotFound : Finished()
        data class Success(val trackMbId: String) : Finished()
        data class Error(val message: String) : Finished()
    }

}