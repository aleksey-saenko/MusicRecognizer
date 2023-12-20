package com.mrsep.musicrecognizer.glue.recognition.adapters

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.data.enqueued.EnqueuedRecognitionRepositoryDo
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionEntityWithTrack
import com.mrsep.musicrecognizer.feature.recognition.domain.EnqueuedRecognitionRepository
import com.mrsep.musicrecognizer.feature.recognition.domain.model.EnqueuedRecognition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject

class AdapterEnqueuedRepository @Inject constructor(
    private val enqueuedRecognitionRepositoryDo: EnqueuedRecognitionRepositoryDo,
    private val enqueuedMapper: BidirectionalMapper<EnqueuedRecognitionEntityWithTrack, EnqueuedRecognition>
) : EnqueuedRecognitionRepository {

    override suspend fun createRecognition(audioRecording: ByteArray, title: String): Int? {
        return enqueuedRecognitionRepositoryDo.create(audioRecording, title)
    }

    override suspend fun getRecognition(recognitionId: Int): EnqueuedRecognition? {
        return enqueuedRecognitionRepositoryDo.getRecognitionWithTrack(recognitionId)
            ?.run(enqueuedMapper::map)
    }

    override suspend fun getRecordingForRecognition(recognitionId: Int): File? {
        return enqueuedRecognitionRepositoryDo.getRecordingForRecognition(recognitionId)
    }

    override fun getAllRecognitionsFlow(): Flow<List<EnqueuedRecognition>> {
        return enqueuedRecognitionRepositoryDo.getAllRecognitionsWithTrackFlow()
            .map { enqueuedList -> enqueuedList.map(enqueuedMapper::map) }
    }

    override suspend fun updateTitle(recognitionId: Int, newTitle: String) {
        enqueuedRecognitionRepositoryDo.updateTitle(recognitionId, newTitle)
    }

    override suspend fun delete(vararg recognitionIds: Int) {
        enqueuedRecognitionRepositoryDo.delete(*recognitionIds)
    }

    override suspend fun deleteAll() {
        enqueuedRecognitionRepositoryDo.deleteAll()
    }

    override suspend fun update(recognition: EnqueuedRecognition) {
        enqueuedRecognitionRepositoryDo.update(
            enqueuedMapper.reverseMap(recognition).enqueued
        )
    }

}