package com.mrsep.musicrecognizer.feature.recognition.domain

import com.mrsep.musicrecognizer.feature.recognition.domain.model.EnqueuedRecognition
import kotlinx.coroutines.flow.Flow
import java.io.File

interface EnqueuedRecognitionRepository {

    suspend fun createRecognition(audioRecording: ByteArray, title: String): Int?

    suspend fun getRecordingForRecognition(recognitionId: Int): File?

    suspend fun getRecognition(recognitionId: Int): EnqueuedRecognition?

    fun getAllRecognitionsFlow(): Flow<List<EnqueuedRecognition>>

    suspend fun update(recognition: EnqueuedRecognition)

    suspend fun updateTitle(recognitionId: Int, newTitle: String)

    suspend fun delete(vararg recognitionIds: Int)

    suspend fun deleteAll()
}
