package com.mrsep.musicrecognizer.feature.recognition.domain.impl

import com.mrsep.musicrecognizer.feature.recognition.domain.EnqueueRecognitionUseCase
import com.mrsep.musicrecognizer.feature.recognition.domain.EnqueuedRecognitionRepository
import javax.inject.Inject

class EnqueueRecognitionUseCaseImpl @Inject constructor(
    private val enqueuedRecognitionRepository: EnqueuedRecognitionRepository
) : EnqueueRecognitionUseCase {

    override suspend operator fun invoke(audioRecording: ByteArray, launch: Boolean) {
        enqueuedRecognitionRepository.createEnqueuedRecognition(audioRecording, launch)
    }

}