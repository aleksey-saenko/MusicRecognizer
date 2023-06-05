package com.mrsep.musicrecognizer.glue.recognitionqueue.adapters

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.enqueued.EnqueuedRecognitionDataRepository
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionData
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.EnqueuedRecognitionRepository
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.model.EnqueuedRecognition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject

class AdapterEnqueuedRepository @Inject constructor(
    private val enqueuedDataRepository: EnqueuedRecognitionDataRepository,
    private val enqueuedToDomainMapper: Mapper<EnqueuedRecognitionData, EnqueuedRecognition>
) : EnqueuedRecognitionRepository {

    override suspend fun updateTitle(enqueuedId: Int, newTitle: String) {
        enqueuedDataRepository.updateTitle(enqueuedId, newTitle)
    }

    override suspend fun getRecordingById(enqueuedId: Int): File? {
        return enqueuedDataRepository.getRecordingById(enqueuedId)
    }

    override suspend fun enqueueById(vararg enqueuedId: Int) {
        enqueuedDataRepository.enqueueById(*enqueuedId)
    }

    override suspend fun cancelById(vararg enqueuedId: Int) {
        enqueuedDataRepository.cancelById(*enqueuedId)
    }

    override suspend fun cancelAndDeleteById(vararg enqueuedId: Int) {
        enqueuedDataRepository.cancelAndDeleteById(*enqueuedId)
    }

    override suspend fun cancelAndDeleteAll() {
        enqueuedDataRepository.cancelAndDeleteAll()
    }

    override fun getAllFlowWithStatus(): Flow<List<EnqueuedRecognition>> {
        return enqueuedDataRepository.getFlowWithStatusAll()
            .map { list ->
                list.map { entityWithStatus ->
                    enqueuedToDomainMapper.map(entityWithStatus)
                }
            }
    }

}