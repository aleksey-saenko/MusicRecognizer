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
    private val enqueuedWorkManager: EnqueuedRecognitionWorkDataManager,
    private val recordingFileDataSource: RecordingFileDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    database: ApplicationDatabase
) : EnqueuedRecognitionDataRepository {

    private val dao = database.enqueuedRecognitionDao()

    override suspend fun createEnqueuedRecognition(audioRecording: ByteArray, launch: Boolean): Int? {
        return withContext(ioDispatcher) {
            recordingFileDataSource.write(audioRecording)?.let { recordingFile ->
                val enqueued = EnqueuedRecognitionEntity(
                    id = 0,
                    title = "",
                    recordFile = recordingFile,
                    creationDate = Instant.now()
                )
                val id = dao.insertOrReplace(enqueued).toInt()
                if (launch) {
                    enqueuedWorkManager.enqueueRecognitionWorker(id)
                }
                id
            }
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
        getById(enqueuedId)?.let { enqueued ->
            enqueuedWorkManager.cancelRecognitionWorker(enqueuedId)
            dao.deleteById(enqueuedId)
            recordingFileDataSource.delete(enqueued.recordFile)
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

    override fun getAllFlow(): Flow<List<EnqueuedRecognitionEntity>> {
        return dao.getFlow()
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
    override fun getAllFlowWithStatus(): Flow<List<EnqueuedRecognitionEntityWithStatus>> {
        return getAllFlow()
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



