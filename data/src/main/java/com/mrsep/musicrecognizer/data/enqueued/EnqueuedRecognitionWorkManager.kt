package com.mrsep.musicrecognizer.data.enqueued

import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionStatusDo
import kotlinx.coroutines.flow.Flow

interface EnqueuedRecognitionWorkManager {

    fun enqueueWorkers(vararg enqueuedId: Int)

    fun cancelWorkers(vararg enqueuedId: Int)

    fun cancelWorkersAll()

    fun getWorkInfoFlowById(enqueuedId: Int): Flow<EnqueuedRecognitionStatusDo>

}