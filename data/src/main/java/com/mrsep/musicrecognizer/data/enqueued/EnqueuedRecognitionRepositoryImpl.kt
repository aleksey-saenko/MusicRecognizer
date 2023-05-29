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
                    enqueuedWorkManager.enqueueRecognitionWorkers(id)
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

    override suspend fun enqueueById(vararg enqueuedId: Int) {
        enqueuedWorkManager.enqueueRecognitionWorkers(*enqueuedId)
    }

    override suspend fun cancelById(vararg enqueuedId: Int) {
        enqueuedWorkManager.cancelRecognitionWorkers(*enqueuedId)
    }

    override suspend fun cancelAndDeleteById(vararg enqueuedId: Int) {
        enqueuedWorkManager.cancelRecognitionWorkers(*enqueuedId)
        withContext(ioDispatcher) {
            val files = dao.getFileRecords(enqueuedId)
            dao.deleteByIds(enqueuedId)
            files.forEach { file -> recordingFileDataSource.delete(file) }
        }
    }

    override suspend fun cancelAndDeleteAll() {
        enqueuedWorkManager.cancelAllRecognitionWorkers()
        withContext(ioDispatcher) {
            recordingFileDataSource.deleteAll()
            dao.deleteAll()
        }
    }

    override suspend fun getById(id: Int): EnqueuedRecognitionEntity? {
        return withContext(ioDispatcher) {
            dao.getById(id)
        }
    }

    override fun getFlowById(id: Int): Flow<EnqueuedRecognitionEntity?> {
        return dao.getFlowById(id)
            .flowOn(ioDispatcher)
    }

    override fun getFlowAll(): Flow<List<EnqueuedRecognitionEntity>> {
        return dao.getFlowAll()
            .flowOn(ioDispatcher)
    }

    override fun getFlowWithStatusById(id: Int): Flow<EnqueuedRecognitionEntityWithStatus?> {
        return dao.getFlowById(id)
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
    override fun getFlowWithStatusAll(): Flow<List<EnqueuedRecognitionEntityWithStatus>> {
        return getFlowAll()
            .flatMapLatest { listEntities ->
                if (listEntities.isEmpty()) {
                    flowOf(emptyList())
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



