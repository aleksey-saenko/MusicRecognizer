package com.mrsep.musicrecognizer.feature.recognition.domain

import com.mrsep.musicrecognizer.feature.recognition.domain.model.ScheduledJobStatus
import kotlinx.coroutines.flow.Flow

internal interface EnqueuedRecognitionScheduler {

    fun enqueue(recognitionIds: List<Int>, forceLaunch: Boolean)

    fun cancel(recognitionIds: List<Int>)

    fun cancelAll()

    fun getJobStatusFlow(recognitionId: Int): Flow<ScheduledJobStatus>

    fun getJobStatusForAllFlow(): Flow<Map<Int, ScheduledJobStatus>>
}
