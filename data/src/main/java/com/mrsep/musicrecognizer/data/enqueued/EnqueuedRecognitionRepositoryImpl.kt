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

internal class EnqueuedRecognitionRepositoryImpl @Inject constructor(
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
                dao.insert(enqueued).toInt()
            }
        }
    }

    override suspend fun update(enqueuedRecognition: EnqueuedRecognitionEntity) {
        withContext(persistentCoroutineContext) {
            dao.update(enqueuedRecognition)
        }
    }

    override suspend fun updateTitle(recognitionId: Int, newTitle: String) {
        withContext(persistentCoroutineContext) {
            dao.updateTitle(recognitionId, newTitle)
        }
    }

    override suspend fun getRecordingForRecognition(recognitionId: Int): File? {
        return withContext(ioDispatcher) {
            dao.getRecordingFile(recognitionId)
        }
    }

    override suspend fun delete(recognitionIds: List<Int>) {
        withContext(persistentCoroutineContext) {
            val files = dao.getRecordingFiles(recognitionIds)
            dao.delete(recognitionIds)
            files.forEach { file -> recordingFileDataSource.delete(file) }
        }
    }

    override suspend fun deleteAll() {
        withContext(persistentCoroutineContext) {
            recordingFileDataSource.deleteAll()
            dao.deleteAll()
        }
    }

    override fun getRecognitionWithTrackFlow(recognitionId: Int): Flow<EnqueuedRecognitionEntityWithTrack?> {
        return dao.getRecognitionWithTrackFlow(recognitionId).flowOn(ioDispatcher)
    }

    override fun getAllRecognitionsWithTrackFlow(): Flow<List<EnqueuedRecognitionEntityWithTrack>> {
        return dao.getAllRecognitionsWithTrackFlow().flowOn(ioDispatcher)
    }
}
