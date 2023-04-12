package com.mrsep.musicrecognizer.data.enqueued

import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.data.database.ApplicationDatabase
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionEntity
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionEntityWithStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import javax.inject.Inject

class EnqueuedRecognitionRepositoryImpl @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val enqueuedWorkManager: EnqueuedRecognitionWorkDataManager,
    database: ApplicationDatabase
) : EnqueuedRecognitionDataRepository {

    private val dao = database.enqueuedRecognitionDao()

    override suspend fun createEnqueuedRecognition(recordFile: File, launch: Boolean) {
        withContext(ioDispatcher) {
            val enqueued = EnqueuedRecognitionEntity(
                id = 0,
                title = "",
                recordFile = recordFile,
                creationDate = Instant.now()
            )
            val id = insertOrReplace(enqueued)
            if (launch) {
                enqueuedWorkManager.enqueueRecognitionWorker(id)
            }
        }
    }

    override suspend fun insertOrReplace(enqueuedRecognition: EnqueuedRecognitionEntity): Int {
        return withContext(ioDispatcher) {
            dao.insertOrReplace(enqueuedRecognition).toInt()
        }
    }

    override suspend fun update(enqueuedRecognition: EnqueuedRecognitionEntity) {
        withContext(ioDispatcher) {
            dao.update(enqueuedRecognition)
        }
    }

    override suspend fun updateTitle(enqueuedId: Int, newTitle: String) {
        withContext(ioDispatcher) {
            dao.updateTitle(enqueuedId, newTitle)
        }
    }

    override suspend fun getRecordById(enqueuedId: Int): File? {
        return withContext(ioDispatcher) {
            dao.getFileRecord(enqueuedId)
        }
    }

    override suspend fun enqueueById(enqueuedId: Int) {
        enqueuedWorkManager.enqueueRecognitionWorker(enqueuedId)
    }

    override suspend fun cancelById(enqueuedId: Int) {
        enqueuedWorkManager.cancelRecognitionWorker(enqueuedId)
    }

    override suspend fun cancelAndDeleteById(enqueuedId: Int) {
        enqueuedWorkManager.cancelRecognitionWorker(enqueuedId)
        deleteById(enqueuedId)
    }

    override suspend fun deleteById(id: Int) {
        withContext(ioDispatcher) {
            dao.deleteById(id)
        }
    }

    override suspend fun getById(id: Int): EnqueuedRecognitionEntity? {
        return withContext(ioDispatcher) {
            dao.getById(id)
        }
    }

    override fun getUniqueFlow(id: Int): Flow<EnqueuedRecognitionEntity?> {
        return dao.getUniqueFlow(id)
            .flowOn(ioDispatcher)
    }

    override fun getAllFlow(limit: Int): Flow<List<EnqueuedRecognitionEntity>> {
        return dao.getFlow(limit)
            .flowOn(ioDispatcher)
    }

    override fun getUniqueFlowWithStatus(id: Int): Flow<EnqueuedRecognitionEntityWithStatus?> {
        return dao.getUniqueFlow(id)
            .combine(enqueuedWorkManager.getUniqueWorkInfoFlow(id)) { entity, status ->
                entity?.let {
                    EnqueuedRecognitionEntityWithStatus(
                        id = entity.id,
                        title = entity.title,
                        recordFile = entity.recordFile,
                        creationDate = entity.creationDate,
                        status = status
                    )
                }
            }
            .flowOn(ioDispatcher)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getAllFlowWithStatus(limit: Int): Flow<List<EnqueuedRecognitionEntityWithStatus>> {
        return getAllFlow(limit)
            .flatMapLatest { listEntities ->
                if (listEntities.isEmpty()) {
                    flow { emit(emptyList()) }
                } else {
                    val listOfFlow = listEntities.map { entity ->
                        enqueuedWorkManager.getUniqueWorkInfoFlow(entity.id)
                            .mapLatest { status ->
                                EnqueuedRecognitionEntityWithStatus(
                                    id = entity.id,
                                    title = entity.title,
                                    recordFile = entity.recordFile,
                                    creationDate = entity.creationDate,
                                    status = status
                                )
                            }
                    }
                    combine(*listOfFlow.toTypedArray()) { array -> array.toList() }
                }
            }
            .flowOn(ioDispatcher)
    }
}



