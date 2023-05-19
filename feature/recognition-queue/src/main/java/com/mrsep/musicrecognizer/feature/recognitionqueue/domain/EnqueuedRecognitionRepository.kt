package com.mrsep.musicrecognizer.feature.recognitionqueue.domain

import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.model.EnqueuedRecognitionWithStatus
import kotlinx.coroutines.flow.Flow
import java.io.File

interface EnqueuedRecognitionRepository {

    suspend fun updateTitle(enqueuedId: Int, newTitle: String)

    suspend fun getRecordById(enqueuedId: Int): File?

    suspend fun enqueueById(enqueuedId: Int)

    suspend fun cancelById(enqueuedId: Int)

    suspend fun cancelAndDeleteById(enqueuedId: Int)

    fun getAllFlowWithStatus(): Flow<List<EnqueuedRecognitionWithStatus>>

}