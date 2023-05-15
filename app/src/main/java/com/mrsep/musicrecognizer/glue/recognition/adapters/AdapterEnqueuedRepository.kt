package com.mrsep.musicrecognizer.glue.recognition.adapters

import com.mrsep.musicrecognizer.data.enqueued.EnqueuedRecognitionDataRepository
import com.mrsep.musicrecognizer.feature.recognition.domain.EnqueuedRecognitionRepository
import javax.inject.Inject

class AdapterEnqueuedRepository @Inject constructor(
    private val enqueuedRecognitionDataRepository: EnqueuedRecognitionDataRepository
) : EnqueuedRecognitionRepository {

    override suspend fun createEnqueuedRecognition(audioRecording: ByteArray, launch: Boolean): Int? {
        return enqueuedRecognitionDataRepository.createEnqueuedRecognition(
            audioRecording = audioRecording,
            launch = launch
        )
    }

}