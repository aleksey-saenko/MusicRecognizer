package com.mrsep.musicrecognizer.data.enqueued

import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionEntity
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionEntityWithTrack
import kotlinx.coroutines.flow.Flow
import java.io.File

interface EnqueuedRecognitionRepositoryDo {

    suspend fun create(audioRecording: ByteArray, title: String): Int?

    suspend fun update(enqueuedRecognition: EnqueuedRecognitionEntity)

    suspend fun updateTitle(enqueuedId: Int, newTitle: String)

    suspend fun deleteById(vararg enqueuedId: Int)

    suspend fun deleteAll()


    suspend fun getRecordingById(enqueuedId: Int): File?

    suspend fun getById(id: Int): EnqueuedRecognitionEntity?

    fun getFlowById(id: Int): Flow<EnqueuedRecognitionEntity?>

    fun getFlowAll(): Flow<List<EnqueuedRecognitionEntity>>


    suspend fun getByIdWithOptionalTrack(id: Int): EnqueuedRecognitionEntityWithTrack?

    suspend fun getAllWithOptionalTrack(): List<EnqueuedRecognitionEntityWithTrack>

    fun getFlowByIdWithOptionalTrack(id: Int): Flow<EnqueuedRecognitionEntityWithTrack?>

    fun getFlowAllWithOptionalTrack(): Flow<List<EnqueuedRecognitionEntityWithTrack>>

}
