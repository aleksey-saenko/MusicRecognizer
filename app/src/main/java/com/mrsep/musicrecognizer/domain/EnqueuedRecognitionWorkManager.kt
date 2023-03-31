package com.mrsep.musicrecognizer.domain

import androidx.work.WorkInfo
import com.mrsep.musicrecognizer.domain.model.EnqueuedRecognition
import kotlinx.coroutines.flow.Flow

interface EnqueuedRecognitionWorkManager {

    fun enqueueRecognitionWorker(enqueuedRecognition: EnqueuedRecognition)

    fun cancelRecognitionWorker(enqueuedRecognition: EnqueuedRecognition)

    fun getUniqueWorkInfoFlow(enqueuedRecognition: EnqueuedRecognition): Flow<WorkInfo?>

    fun getAllWorkInfoFlow(): Flow<List<WorkInfo>>

}