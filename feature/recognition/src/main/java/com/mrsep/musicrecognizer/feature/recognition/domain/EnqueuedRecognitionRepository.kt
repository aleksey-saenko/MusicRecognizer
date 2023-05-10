package com.mrsep.musicrecognizer.feature.recognition.domain

interface EnqueuedRecognitionRepository {

    suspend fun createEnqueuedRecognition(audioRecording: ByteArray, launch: Boolean): Boolean

}