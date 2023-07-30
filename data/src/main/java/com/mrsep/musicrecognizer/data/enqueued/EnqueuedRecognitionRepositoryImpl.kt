package com.mrsep.musicrecognizer.data.enqueued

import com.mrsep.musicrecognizer.core.common.di.ApplicationScope
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.data.database.ApplicationDatabase
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionEntity
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionEntityWithTrack
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import javax.inject.Inject

class EnqueuedRecognitionRepositoryImpl @Inject constructor(
    private val recordingFileDataSource: RecordingFileDataSource,
    @ApplicationScope private val appScope: CoroutineScope,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    database: ApplicationDatabase
) : EnqueuedRecognitionRepositoryDo {

    private val dao = database.enqueuedRecognitionDao()
    private val persistentCoroutineContext = appScope.coroutineContext + ioDispatcher

    override suspend fun create(audioRecording: ByteArray, title: String): Int? {
        return withContext(persistentCoroutineContext) {
            recordingFileDataSource.write(audioRecording)?.let { recordingFile ->
                val enqueued = EnqueuedRecognitionEntity(
                    id = 0,
                    title = title,
                    recordFile = recordingFile,
                    creationDate = Instant.now()
                )
                dao.insertOrReplace(enqueued).toInt()
            }
        }
    }

    override suspend fun update(enqueuedRecognition: EnqueuedRecognitionEntity) {
        withContext(persistentCoroutineContext) {
            dao.update(enqueuedRecognition)
        }
    }

    override suspend fun updateTitle(enqueuedId: Int, newTitle: String) {
        withContext(persistentCoroutineContext) {
            dao.updateTitle(enqueuedId, newTitle)
        }
    }

    override suspend fun getRecordingById(enqueuedId: Int): File? {
        return withContext(ioDispatcher) {
            dao.getRecordingFile(enqueuedId)
        }
    }

    override suspend fun getById(id: Int): EnqueuedRecognitionEntity? {
        return withContext(ioDispatcher) {
            dao.getById(id)
        }
    }

    override fun getFlowById(id: Int): Flow<EnqueuedRecognitionEntity?> {
        return dao.getFlowById(id).flowOn(ioDispatcher)
    }

    override fun getFlowAll(): Flow<List<EnqueuedRecognitionEntity>> {
        return dao.getFlowAll().flowOn(ioDispatcher)
    }

    override suspend fun deleteById(vararg enqueuedId: Int) {
        withContext(persistentCoroutineContext) {
            val files = dao.getRecordingFiles(*enqueuedId)
            dao.deleteById(*enqueuedId)
            files.forEach { file -> recordingFileDataSource.delete(file) }
        }
    }

    override suspend fun deleteAll() {
        withContext(persistentCoroutineContext) {
            recordingFileDataSource.deleteAll()
            dao.deleteAll()
        }
    }


    override suspend fun getByIdWithOptionalTrack(id: Int): EnqueuedRecognitionEntityWithTrack? {
        return withContext(ioDispatcher) {
            dao.getByIdWithOptionalTrack(id).firstOrNull()
        }
    }

    override suspend fun getAllWithOptionalTrack(): List<EnqueuedRecognitionEntityWithTrack> {
        return withContext(ioDispatcher) {
            dao.getAllWithOptionalTrackAll()
        }
    }

    override fun getFlowByIdWithOptionalTrack(id: Int): Flow<EnqueuedRecognitionEntityWithTrack?> {
        return dao.getFlowByIdWithOptionalTrack(id).flowOn(ioDispatcher)
    }

    override fun getFlowAllWithOptionalTrack(): Flow<List<EnqueuedRecognitionEntityWithTrack>> {
        return dao.getFlowAllWithOptionalTrack().flowOn(ioDispatcher)
    }

}



