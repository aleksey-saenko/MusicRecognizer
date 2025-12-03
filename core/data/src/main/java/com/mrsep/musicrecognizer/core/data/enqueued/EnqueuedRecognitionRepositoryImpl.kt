package com.mrsep.musicrecognizer.core.data.enqueued

import com.mrsep.musicrecognizer.core.common.di.ApplicationScope
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.core.database.ApplicationDatabase
import com.mrsep.musicrecognizer.core.database.enqueued.model.EnqueuedRecognitionEntity
import com.mrsep.musicrecognizer.core.database.enqueued.model.EnqueuedRecognitionEntityWithTrack
import com.mrsep.musicrecognizer.core.domain.recognition.AudioSample
import com.mrsep.musicrecognizer.core.domain.recognition.EnqueuedRecognitionRepository
import com.mrsep.musicrecognizer.core.domain.recognition.model.EnqueuedRecognition
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

internal class EnqueuedRecognitionRepositoryImpl @Inject constructor(
    private val audioSampleDataSource: AudioSampleDataSource,
    @ApplicationScope private val appScope: CoroutineScope,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    database: ApplicationDatabase
) : EnqueuedRecognitionRepository {

    private val dao = database.enqueuedRecognitionDao()
    private val persistentCoroutineContext = appScope.coroutineContext + ioDispatcher

    override suspend fun createRecognition(sample: AudioSample, title: String): Int? {
        return withContext(persistentCoroutineContext) {
            audioSampleDataSource.copy(sample)?.let { sample ->
                val enqueued = EnqueuedRecognitionEntity(
                    id = 0,
                    title = title,
                    recordFile = sample.file,
                    creationDate = sample.timestamp
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

    override suspend fun getAudioSampleFile(recognitionId: Int): File? {
        return withContext(ioDispatcher) {
            dao.getRecordingFile(recognitionId)
        }
    }

    override suspend fun getAudioSample(recognitionId: Int): AudioSample? {
        return withContext(ioDispatcher) {
            dao.getRecognition(recognitionId)?.let { recognition ->
                audioSampleDataSource.read(recognition.recordFile, recognition.creationDate)
            }
        }
    }

    override suspend fun delete(recognitionIds: List<Int>) {
        withContext(persistentCoroutineContext) {
            val files = dao.getRecordingFiles(recognitionIds)
            dao.delete(recognitionIds)
            files.forEach { file -> audioSampleDataSource.delete(file) }
        }
    }

    override suspend fun deleteAll() {
        withContext(persistentCoroutineContext) {
            audioSampleDataSource.deleteAll()
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
