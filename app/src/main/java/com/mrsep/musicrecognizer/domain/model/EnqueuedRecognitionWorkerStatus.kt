package com.mrsep.musicrecognizer.domain.model

sealed class EnqueuedRecognitionWorkerStatus {

    object Inactive : EnqueuedRecognitionWorkerStatus()
    object Enqueued : EnqueuedRecognitionWorkerStatus()
    object Running : EnqueuedRecognitionWorkerStatus()
    object Canceled : EnqueuedRecognitionWorkerStatus()

    sealed class Finished : EnqueuedRecognitionWorkerStatus() {
        object NotFound : Finished()
        data class Success(val trackMbId: String) : Finished()
        data class Error(val message: String) : Finished()
    }

}