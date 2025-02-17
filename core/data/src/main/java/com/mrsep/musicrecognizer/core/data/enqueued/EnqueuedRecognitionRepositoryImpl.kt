package com.mrsep.musicrecognizer.core.data.enqueued

import com.mrsep.musicrecognizer.core.common.di.ApplicationScope
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.core.database.ApplicationDatabase
import com.mrsep.musicrecognizer.core.database.enqueued.model.EnqueuedRecognitionEntity
import com.mrsep.musicrecognizer.core.database.enqueued.model.EnqueuedRecognitionEntityWithTrack
import com.mrsep.musicrecognizer.core.domain.recognition.EnqueuedRecognitionRepository
import com.mrsep.musicrecognizer.core.domain.recognition.model.EnqueuedRecognition
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
) : EnqueuedRecognitionRepository {

    private val dao = database.enqueuedRecognitionDao()
    private val persistentCoroutineContext = appScope.coroutineContext + ioDispatcher

    override suspend fun createRecognition(audioRecording: ByteArray, title: String): Int? {
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

    override suspend fun update(recognition: EnqueuedRecognition) {
        withContext(persistentCoroutineContext) {
            dao.update(recognition.toEntity())
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

    override fun getRecognitionFlow(recognitionId: Int): Flow<EnqueuedRecognition?> {
        return dao.getRecognitionWithTrackFlow(recognitionId)
            .map { entity -> entity?.toDomain() }
            .flowOn(ioDispatcher)
    }

    override fun getAllRecognitionsFlow(): Flow<List<EnqueuedRecognition>> {
        return dao.getAllRecognitionsWithTrackFlow()
            .map { list -> list.map(EnqueuedRecognitionEntityWithTrack::toDomain) }
            .flowOn(ioDispatcher)
    }
}
