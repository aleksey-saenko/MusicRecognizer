package com.mrsep.musicrecognizer.core.domain.recognition

import com.mrsep.musicrecognizer.core.domain.recognition.model.EnqueuedRecognition
import kotlinx.coroutines.flow.Flow
import java.io.File

interface EnqueuedRecognitionRepository {

    suspend fun update(recognition: EnqueuedRecognition)

    suspend fun updateTitle(recognitionId: Int, newTitle: String)

    suspend fun delete(recognitionIds: List<Int>)

    suspend fun deleteAll()

    suspend fun createRecognition(audioRecording: AudioRecording, title: String): Int?

    suspend fun getRecordingFile(recognitionId: Int): File?

    suspend fun getRecording(recognitionId: Int): AudioRecording?

    fun getRecognitionFlow(recognitionId: Int): Flow<EnqueuedRecognition?>

    fun getAllRecognitionsFlow(): Flow<List<EnqueuedRecognition>>
}
