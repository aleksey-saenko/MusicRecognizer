package com.mrsep.musicrecognizer.data.enqueued.model

import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionDataResult

sealed class EnqueuedRecognitionDataStatus {

    // worker is not launched
    object Inactive : EnqueuedRecognitionDataStatus()

    object Enqueued : EnqueuedRecognitionDataStatus()

    object Running : EnqueuedRecognitionDataStatus()

    data class Finished(
        val remoteResult: RemoteRecognitionDataResult
    ) : EnqueuedRecognitionDataStatus()

}