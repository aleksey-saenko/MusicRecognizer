package com.mrsep.musicrecognizer.core.domain.recognition

import com.mrsep.musicrecognizer.core.domain.recognition.model.ScheduledJobStatus
import kotlinx.coroutines.flow.Flow

interface EnqueuedRecognitionScheduler {

    fun enqueue(recognitionIds: List<Int>, forceLaunch: Boolean)

    fun cancel(recognitionIds: List<Int>)

    fun cancelAll()

    fun getJobStatusFlow(recognitionId: Int): Flow<ScheduledJobStatus>

    fun getJobStatusForAllFlow(): Flow<Map<Int, ScheduledJobStatus>>
}
