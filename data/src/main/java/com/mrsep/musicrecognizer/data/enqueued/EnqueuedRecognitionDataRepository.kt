package com.mrsep.musicrecognizer.data.enqueued

import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionEntity
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionEntityWithStatus
import kotlinx.coroutines.flow.Flow
import java.io.File

interface EnqueuedRecognitionDataRepository {

    suspend fun createEnqueuedRecognition(audioRecording: ByteArray, launch: Boolean): Boolean

    suspend fun update(enqueuedRecognition: EnqueuedRecognitionEntity)

    suspend fun updateTitle(enqueuedId: Int, newTitle: String)

    suspend fun getRecordById(enqueuedId: Int): File?

    suspend fun enqueueById(enqueuedId: Int)

    suspend fun cancelById(enqueuedId: Int)

    suspend fun cancelAndDeleteById(enqueuedId: Int)

    suspend fun getById(id: Int): EnqueuedRecognitionEntity?

    fun getUniqueFlow(id: Int): Flow<EnqueuedRecognitionEntity?>

    fun getAllFlow(limit: Int): Flow<List<EnqueuedRecognitionEntity>>

    fun getUniqueFlowWithStatus(id: Int): Flow<EnqueuedRecognitionEntityWithStatus?>

    fun getAllFlowWithStatus(limit: Int): Flow<List<EnqueuedRecognitionEntityWithStatus>>


}
