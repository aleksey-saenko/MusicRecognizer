package com.mrsep.musicrecognizer.feature.recognition.domain

import com.mrsep.musicrecognizer.feature.recognition.domain.model.EnqueuedRecognition
import kotlinx.coroutines.flow.Flow
import java.io.File

interface EnqueuedRecognitionRepository {

    suspend fun createEnqueuedRecognition(audioRecording: ByteArray, title: String): Int?

    suspend fun getRecordingById(enqueuedId: Int): File?

    suspend fun getById(id: Int): EnqueuedRecognition?

    fun getFlowAll(): Flow<List<EnqueuedRecognition>>

    suspend fun update(enqueuedRecognition: EnqueuedRecognition)

    suspend fun updateTitle(enqueuedId: Int, newTitle: String)

    suspend fun deleteById(vararg enqueuedId: Int)

    suspend fun deleteAll()

}