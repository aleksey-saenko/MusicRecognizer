package com.mrsep.musicrecognizer.feature.recognitionqueue.domain

import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.model.EnqueuedRecognitionWithStatus
import kotlinx.coroutines.flow.Flow
import java.io.File

interface EnqueuedRecognitionRepository {

    suspend fun updateTitle(enqueuedId: Int, newTitle: String)

    suspend fun getRecordById(enqueuedId: Int): File?

    suspend fun enqueueById(vararg enqueuedId: Int)

    suspend fun cancelById(vararg enqueuedId: Int)

    suspend fun cancelAndDeleteById(vararg enqueuedId: Int)

    suspend fun cancelAndDeleteAll()

    fun getAllFlowWithStatus(): Flow<List<EnqueuedRecognitionWithStatus>>

}