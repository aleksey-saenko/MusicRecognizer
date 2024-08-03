package com.mrsep.musicrecognizer.feature.recognition.domain

import com.mrsep.musicrecognizer.feature.recognition.domain.model.EnqueuedRecognition
import kotlinx.coroutines.flow.Flow
import java.io.File

interface EnqueuedRecognitionRepository {

    suspend fun update(recognition: EnqueuedRecognition)

    suspend fun updateTitle(recognitionId: Int, newTitle: String)

    suspend fun delete(recognitionIds: List<Int>)

    suspend fun deleteAll()

    suspend fun createRecognition(audioRecording: ByteArray, title: String): Int?

    suspend fun getRecordingForRecognition(recognitionId: Int): File?

    fun getRecognitionFlow(recognitionId: Int): Flow<EnqueuedRecognition?>

    fun getAllRecognitionsFlow(): Flow<List<EnqueuedRecognition>>
}
