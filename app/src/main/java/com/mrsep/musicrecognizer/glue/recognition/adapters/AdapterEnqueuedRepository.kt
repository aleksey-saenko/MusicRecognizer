package com.mrsep.musicrecognizer.glue.recognition.adapters

import com.mrsep.musicrecognizer.data.enqueued.EnqueuedRecognitionRepositoryDo
import com.mrsep.musicrecognizer.feature.recognition.domain.EnqueuedRecognitionRepository
import javax.inject.Inject

class AdapterEnqueuedRepository @Inject constructor(
    private val enqueuedRecognitionRepositoryDo: EnqueuedRecognitionRepositoryDo
) : EnqueuedRecognitionRepository {

    override suspend fun createEnqueuedRecognition(audioRecording: ByteArray, launch: Boolean): Int? {
        return enqueuedRecognitionRepositoryDo.createEnqueuedRecognition(
            audioRecording = audioRecording,
            launch = launch
        )
    }

}