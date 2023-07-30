package com.mrsep.musicrecognizer.feature.recognition.domain

import com.mrsep.musicrecognizer.feature.recognition.domain.model.ScheduledJobStatus
import kotlinx.coroutines.flow.Flow

internal interface EnqueuedRecognitionScheduler {

    fun enqueueById(vararg enqueuedId: Int)

    fun cancelById(vararg enqueuedId: Int)

    fun cancelAll()

    fun getStatusFlowById(enqueuedId: Int): Flow<ScheduledJobStatus>

    fun getStatusFlowAll(): Flow<Map<Int, ScheduledJobStatus>>

}