package com.mrsep.musicrecognizer.feature.recognition.domain

import com.mrsep.musicrecognizer.feature.recognition.domain.model.ScheduledJobStatus
import kotlinx.coroutines.flow.Flow

internal interface EnqueuedRecognitionScheduler {

    fun enqueue(vararg recognitionIds: Int, forceLaunch: Boolean)

    fun cancel(vararg recognitionIds: Int)

    fun cancelAll()

    fun getJobStatusFlow(recognitionId: Int): Flow<ScheduledJobStatus>

    fun getJobStatusForAllFlow(): Flow<Map<Int, ScheduledJobStatus>>

}