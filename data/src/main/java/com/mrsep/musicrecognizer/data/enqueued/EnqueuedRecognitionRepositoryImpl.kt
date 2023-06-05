package com.mrsep.musicrecognizer.data.enqueued

import com.mrsep.musicrecognizer.core.common.di.ApplicationScope
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.data.database.ApplicationDatabase
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionDataStatus
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionEntity
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionData
import com.mrsep.musicrecognizer.data.enqueued.model.RemoteRecognitionResultType
import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionDataResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import javax.inject.Inject

class EnqueuedRecognitionRepositoryImpl @Inject constructor(
    private val enqueuedWorkManager: EnqueuedRecognitionWorkDataManager,
    private val recordingFileDataSource: RecordingFileDataSource,
    @ApplicationScope private val appScope: CoroutineScope,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    database: ApplicationDatabase
) : EnqueuedRecognitionDataRepository {

    private val dao = database.enqueuedRecognitionDao()

    override suspend fun createEnqueuedRecognition(
        audioRecording: ByteArray,
        launch: Boolean
    ): Int? {
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
                    enqueuedWorkManager.enqueueWorkers(id)
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

    override suspend fun getRecordingById(enqueuedId: Int): File? {
        return withContext(ioDispatcher) {
            dao.getRecordingFile(enqueuedId)
        }
    }

    override suspend fun enqueueById(vararg enqueuedId: Int) {
        withContext(appScope.coroutineContext + ioDispatcher) {
            val enqueuedWithResetResult = dao.getByIds(*enqueuedId).map { enqueued ->
                enqueued.copy(
                    resultType = null,
                    resultMbId = null,
                    resultMessage = null,
                    resultDate = null
                )
            }.toTypedArray()
            dao.update(*enqueuedWithResetResult)
            enqueuedWorkManager.enqueueWorkers(*enqueuedId)
        }
    }

    override suspend fun cancelById(vararg enqueuedId: Int) {
        enqueuedWorkManager.cancelWorkers(*enqueuedId)
    }

    override suspend fun cancelAndDeleteById(vararg enqueuedId: Int) {
        withContext(appScope.coroutineContext + ioDispatcher) {
            enqueuedWorkManager.cancelWorkers(*enqueuedId)
            val files = dao.getRecordingFiles(*enqueuedId)
            dao.deleteById(*enqueuedId)
            files.forEach { file -> recordingFileDataSource.delete(file) }
        }
    }

    override suspend fun cancelAndDeleteAll() {
        withContext(appScope.coroutineContext + ioDispatcher) {
            enqueuedWorkManager.cancelWorkersAll()
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

    override fun getFlowWithStatusById(id: Int): Flow<EnqueuedRecognitionData?> {
        return dao.getFlowById(id)
            .combine(enqueuedWorkManager.getWorkInfoFlowById(id)) { entity, status ->
                entity?.let {
                    EnqueuedRecognitionData(
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
    override fun getFlowWithStatusAll(): Flow<List<EnqueuedRecognitionData>> {
        return getFlowAll()
            .flatMapLatest { listEntities ->
                if (listEntities.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    val listOfFlow = listEntities.map { enqueuedEntity ->
                        enqueuedWorkManager.getWorkInfoFlowById(enqueuedEntity.id)
                            .mapLatest { workerStatus ->
                                if (workerStatus is EnqueuedRecognitionDataStatus.Inactive) {
                                    val storedStatus = enqueuedEntity.getStatusOrNull()
                                    enqueuedEntity.combineEnqueued(storedStatus ?: workerStatus)
                                } else {
                                    enqueuedEntity.combineEnqueued(workerStatus)
                                }
                            }
                    }
                    combine(*listOfFlow.toTypedArray()) { array -> array.toList() }
                }
            }
            .flowOn(ioDispatcher)
    }

    private fun EnqueuedRecognitionEntity.combineEnqueued(
        status: EnqueuedRecognitionDataStatus
    ): EnqueuedRecognitionData {
        return EnqueuedRecognitionData(
            id = this.id,
            title = this.title,
            recordFile = this.recordFile,
            creationDate = this.creationDate,
            status = status
        )
    }

    //FIXME: should be replace by mapper + see function in worker
    private fun EnqueuedRecognitionEntity.getStatusOrNull(): EnqueuedRecognitionDataStatus? {
        val remoteResult = resultType?.let { resultType ->
            when (resultType) {
                RemoteRecognitionResultType.Success -> {
                    dao.getEnqueuedWithOptionalTrackById(id).firstOrNull()
                        ?.let { enqueuedWithTrack ->
                            enqueuedWithTrack.track?.let { track ->
                                RemoteRecognitionDataResult.Success(track)
                            }
                        }
                }

                RemoteRecognitionResultType.NoMatches -> {
                    RemoteRecognitionDataResult.NoMatches
                }

                RemoteRecognitionResultType.BadConnection -> {
                    RemoteRecognitionDataResult.Error.BadConnection
                }

                RemoteRecognitionResultType.BadRecording -> {
                    RemoteRecognitionDataResult.Error.BadRecording(resultMessage ?: "")
                }

                RemoteRecognitionResultType.WrongToken -> {
                    RemoteRecognitionDataResult.Error.WrongToken(false)
                }

                RemoteRecognitionResultType.LimitedToken -> {
                    RemoteRecognitionDataResult.Error.WrongToken(true)
                }

                RemoteRecognitionResultType.HttpError -> {
                    // assuming the pattern "${code}\n${message}"
                    val data = resultMessage?.split("\n", limit = 2)
                    RemoteRecognitionDataResult.Error.HttpError(
                        code = data?.get(0)?.toIntOrNull() ?: -1,
                        message = data?.get(1) ?: ""
                    )
                }

                RemoteRecognitionResultType.UnhandledError -> {
                    RemoteRecognitionDataResult.Error.UnhandledError(
                        message = resultMessage ?: ""
                    )
                }
            }
        }
        return remoteResult?.let { result -> EnqueuedRecognitionDataStatus.Finished(result) }
    }


}



