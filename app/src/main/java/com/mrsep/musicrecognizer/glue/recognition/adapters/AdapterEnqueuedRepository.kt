package com.mrsep.musicrecognizer.glue.recognition.adapters

import com.mrsep.musicrecognizer.data.enqueued.EnqueuedRecognitionDataRepository
import com.mrsep.musicrecognizer.feature.recognition.domain.EnqueuedRecognitionRepository
import java.io.File
import javax.inject.Inject

class AdapterEnqueuedRepository @Inject constructor(
    private val enqueuedRecognitionDataRepository: EnqueuedRecognitionDataRepository
) : EnqueuedRecognitionRepository {

    override suspend fun createEnqueuedRecognition(recordFile: File, launch: Boolean) {
        enqueuedRecognitionDataRepository.createEnqueuedRecognition(recordFile, launch)
    }

}