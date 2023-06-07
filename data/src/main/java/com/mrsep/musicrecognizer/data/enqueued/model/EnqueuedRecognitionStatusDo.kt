package com.mrsep.musicrecognizer.data.enqueued.model

import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionResultDo

sealed class EnqueuedRecognitionStatusDo {

    // worker is not launched
    object Inactive : EnqueuedRecognitionStatusDo()

    object Enqueued : EnqueuedRecognitionStatusDo()

    object Running : EnqueuedRecognitionStatusDo()

    data class Finished(
        val remoteResult: RemoteRecognitionResultDo
    ) : EnqueuedRecognitionStatusDo()

}