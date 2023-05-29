package com.mrsep.musicrecognizer.data.enqueued

import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionDataStatus
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionDataStatusWithId
import kotlinx.coroutines.flow.Flow

interface EnqueuedRecognitionWorkDataManager {
    fun enqueueRecognitionWorkers(vararg enqueuedId: Int)
    fun cancelRecognitionWorkers(vararg enqueuedId: Int)
    fun cancelAllRecognitionWorkers()
    fun getUniqueWorkInfoFlow(enqueuedId: Int): Flow<EnqueuedRecognitionDataStatus>
    fun getAllWorkInfoFlow(): Flow<List<EnqueuedRecognitionDataStatusWithId>>
}