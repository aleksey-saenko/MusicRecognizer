package com.mrsep.musicrecognizer.feature.recognition.domain

import java.io.File

interface EnqueuedRecognitionRepository {

    suspend fun createEnqueuedRecognition(recordFile: File, launch: Boolean)

}