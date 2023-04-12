package com.mrsep.musicrecognizer.glue.recognitionqueue.adapters

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.enqueued.EnqueuedRecognitionDataRepository
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionEntityWithStatus
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.EnqueuedRecognitionRepository
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.model.EnqueuedRecognitionWithStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject

class AdapterEnqueuedRepository @Inject constructor(
    private val enqueuedDataRepository: EnqueuedRecognitionDataRepository,
    private val enqueuedToDomainMapper: Mapper<EnqueuedRecognitionEntityWithStatus, EnqueuedRecognitionWithStatus>
) : EnqueuedRecognitionRepository {

    override suspend fun updateTitle(enqueuedId: Int, newTitle: String) {
        enqueuedDataRepository.updateTitle(enqueuedId, newTitle)
    }

    override suspend fun getRecordById(enqueuedId: Int): File? {
        return enqueuedDataRepository.getRecordById(enqueuedId)
    }

    override suspend fun enqueueById(enqueuedId: Int) {
        enqueuedDataRepository.enqueueById(enqueuedId)
    }

    override suspend fun cancelById(enqueuedId: Int) {
        enqueuedDataRepository.cancelById(enqueuedId)
    }

    override suspend fun cancelAndDeleteById(enqueuedId: Int) {
        enqueuedDataRepository.cancelAndDeleteById(enqueuedId)
    }

    override fun getAllFlowWithStatus(limit: Int): Flow<List<EnqueuedRecognitionWithStatus>> {
        return enqueuedDataRepository.getAllFlowWithStatus(limit)
            .map { list -> list.map { entityWithStatus -> enqueuedToDomainMapper.map(entityWithStatus) } }
    }

}