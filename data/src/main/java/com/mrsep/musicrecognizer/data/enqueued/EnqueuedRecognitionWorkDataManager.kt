package com.mrsep.musicrecognizer.data.enqueued

import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionDataStatus
import kotlinx.coroutines.flow.Flow

interface EnqueuedRecognitionWorkDataManager {

    fun enqueueWorkers(vararg enqueuedId: Int)

    fun cancelWorkers(vararg enqueuedId: Int)

    fun cancelWorkersAll()

    fun getWorkInfoFlowById(enqueuedId: Int): Flow<EnqueuedRecognitionDataStatus>

}