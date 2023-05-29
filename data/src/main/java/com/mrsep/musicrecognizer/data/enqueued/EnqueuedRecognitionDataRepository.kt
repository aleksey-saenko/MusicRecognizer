package com.mrsep.musicrecognizer.data.enqueued

import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionEntity
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionEntityWithStatus
import kotlinx.coroutines.flow.Flow
import java.io.File

interface EnqueuedRecognitionDataRepository {

    suspend fun createEnqueuedRecognition(audioRecording: ByteArray, launch: Boolean): Int?

    suspend fun update(enqueuedRecognition: EnqueuedRecognitionEntity)

    suspend fun updateTitle(enqueuedId: Int, newTitle: String)

    suspend fun getRecordById(enqueuedId: Int): File?

    suspend fun enqueueById(vararg enqueuedId: Int)

    suspend fun cancelById(vararg enqueuedId: Int)

    suspend fun cancelAndDeleteById(vararg enqueuedId: Int)

    suspend fun cancelAndDeleteAll()

    suspend fun getById(id: Int): EnqueuedRecognitionEntity?

    fun getFlowById(id: Int): Flow<EnqueuedRecognitionEntity?>

    fun getFlowAll(): Flow<List<EnqueuedRecognitionEntity>>

    fun getFlowWithStatusById(id: Int): Flow<EnqueuedRecognitionEntityWithStatus?>

    fun getFlowWithStatusAll(): Flow<List<EnqueuedRecognitionEntityWithStatus>>


}
