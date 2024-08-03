package com.mrsep.musicrecognizer.data.enqueued

import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionEntity
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionEntityWithTrack
import kotlinx.coroutines.flow.Flow
import java.io.File

interface EnqueuedRecognitionRepositoryDo {

    suspend fun create(audioRecording: ByteArray, title: String): Int?

    suspend fun update(enqueuedRecognition: EnqueuedRecognitionEntity)

    suspend fun updateTitle(recognitionId: Int, newTitle: String)

    suspend fun delete(recognitionIds: List<Int>)

    suspend fun deleteAll()

    suspend fun getRecordingForRecognition(recognitionId: Int): File?

    fun getRecognitionWithTrackFlow(recognitionId: Int): Flow<EnqueuedRecognitionEntityWithTrack?>

    fun getAllRecognitionsWithTrackFlow(): Flow<List<EnqueuedRecognitionEntityWithTrack>>
}
