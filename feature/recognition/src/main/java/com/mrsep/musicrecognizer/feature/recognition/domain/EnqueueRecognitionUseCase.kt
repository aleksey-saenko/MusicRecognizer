package com.mrsep.musicrecognizer.feature.recognition.domain

interface EnqueueRecognitionUseCase {

    suspend operator fun invoke(audioRecording: ByteArray, launch: Boolean)

}