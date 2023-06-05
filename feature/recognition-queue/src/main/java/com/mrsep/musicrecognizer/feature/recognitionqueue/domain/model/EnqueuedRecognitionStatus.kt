package com.mrsep.musicrecognizer.feature.recognitionqueue.domain.model

sealed class EnqueuedRecognitionStatus {

    object Inactive : EnqueuedRecognitionStatus()
    object Enqueued : EnqueuedRecognitionStatus()
    object Running : EnqueuedRecognitionStatus()

    data class Finished(
        val remoteResult: RemoteRecognitionResult
    ) : EnqueuedRecognitionStatus()

}