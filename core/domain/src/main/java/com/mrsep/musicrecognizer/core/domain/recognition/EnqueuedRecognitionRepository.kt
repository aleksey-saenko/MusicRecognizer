package com.mrsep.musicrecognizer.core.domain.recognition

import com.mrsep.musicrecognizer.core.domain.recognition.model.EnqueuedRecognition
import kotlinx.coroutines.flow.Flow
import java.io.File

interface EnqueuedRecognitionRepository {

    suspend fun update(recognition: EnqueuedRecognition)

    suspend fun updateTitle(recognitionId: Int, newTitle: String)

    suspend fun delete(recognitionIds: List<Int>)

    suspend fun deleteAll()

    suspend fun createRecognition(sample: AudioSample, title: String): Int?

    suspend fun getAudioSampleFile(recognitionId: Int): File?

    suspend fun getAudioSample(recognitionId: Int): AudioSample?

    fun getRecognitionFlow(recognitionId: Int): Flow<EnqueuedRecognition?>

    fun getAllRecognitionsFlow(): Flow<List<EnqueuedRecognition>>
}
