package com.mrsep.musicrecognizer.data.enqueued

import com.mrsep.musicrecognizer.data.database.ApplicationDatabase
import com.mrsep.musicrecognizer.di.IoDispatcher
import com.mrsep.musicrecognizer.domain.EnqueuedRecognitionRepository
import com.mrsep.musicrecognizer.domain.model.EnqueuedRecognition
import com.mrsep.musicrecognizer.domain.model.Mapper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnqueuedRecognitionRepositoryImpl @Inject constructor(
    private val enqueuedToDomainMapper: Mapper<EnqueuedRecognitionEntity, EnqueuedRecognition>,
    private val enqueuedToDataMapper: Mapper<EnqueuedRecognition, EnqueuedRecognitionEntity>,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    database: ApplicationDatabase
) : EnqueuedRecognitionRepository {

    private val dao = database.enqueuedRecognitionDao()

    override suspend fun insertOrReplace(enqueuedRecognition: EnqueuedRecognition): Int {
        return withContext(ioDispatcher) {
            dao.insertOrReplace(enqueuedToDataMapper.map(enqueuedRecognition)).toInt()
        }
    }

    override suspend fun update(enqueuedRecognition: EnqueuedRecognition) {
        withContext(ioDispatcher) {
            dao.update(enqueuedToDataMapper.map(enqueuedRecognition))
        }
    }

    override suspend fun deleteById(id: Int) {
        withContext(ioDispatcher) {
            dao.deleteById(id)
        }
    }

    override suspend fun getById(id: Int): EnqueuedRecognition? {
        return withContext(ioDispatcher) {
            dao.getById(id)?.let { enqueuedToDomainMapper.map(it) }
        }
    }

    override fun getFlow(limit: Int): Flow<List<EnqueuedRecognition>> {
        return dao.getFlow(limit)
            .map { list -> list.map { enqueuedEntity -> enqueuedToDomainMapper.map(enqueuedEntity) } }
            .flowOn(ioDispatcher)
    }

}



