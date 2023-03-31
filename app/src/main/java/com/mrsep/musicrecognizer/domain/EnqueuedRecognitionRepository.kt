package com.mrsep.musicrecognizer.domain

import com.mrsep.musicrecognizer.domain.model.EnqueuedRecognition
import kotlinx.coroutines.flow.Flow

interface EnqueuedRecognitionRepository {

    suspend fun insertOrReplace(enqueuedRecognition: EnqueuedRecognition): Int

    suspend fun update(enqueuedRecognition: EnqueuedRecognition)

    suspend fun deleteById(id: Int)

    suspend fun getById(id: Int): EnqueuedRecognition?

    fun getFlow(limit: Int): Flow<List<EnqueuedRecognition>>

}