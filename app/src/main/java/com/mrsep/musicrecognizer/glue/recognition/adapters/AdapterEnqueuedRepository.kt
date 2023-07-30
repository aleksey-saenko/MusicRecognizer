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

    override suspend fun createEnqueuedRecognition(audioRecording: ByteArray, title: String): Int? {
        return enqueuedRecognitionRepositoryDo.create(audioRecording, title)
    }

    override suspend fun getById(id: Int): EnqueuedRecognition? {
        return enqueuedRecognitionRepositoryDo.getByIdWithOptionalTrack(id)?.run(enqueuedMapper::map)
    }

    override suspend fun getRecordingById(enqueuedId: Int): File? {
        return enqueuedRecognitionRepositoryDo.getRecordingById(enqueuedId)
    }

    override fun getFlowAll(): Flow<List<EnqueuedRecognition>> {
        return enqueuedRecognitionRepositoryDo.getFlowAllWithOptionalTrack()
            .map { enqueuedList -> enqueuedList.map(enqueuedMapper::map) }
    }

    override suspend fun updateTitle(enqueuedId: Int, newTitle: String) {
        enqueuedRecognitionRepositoryDo.updateTitle(enqueuedId, newTitle)
    }

    override suspend fun deleteById(vararg enqueuedId: Int) {
        enqueuedRecognitionRepositoryDo.deleteById(*enqueuedId)
    }

    override suspend fun deleteAll() {
        enqueuedRecognitionRepositoryDo.deleteAll()
    }

    override suspend fun update(enqueuedRecognition: EnqueuedRecognition) {
        enqueuedRecognitionRepositoryDo.update(
            enqueuedMapper.reverseMap(enqueuedRecognition).enqueued
        )
    }

}