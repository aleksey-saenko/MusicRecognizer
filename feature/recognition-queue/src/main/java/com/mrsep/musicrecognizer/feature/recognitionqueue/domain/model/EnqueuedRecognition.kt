package com.mrsep.musicrecognizer.feature.recognitionqueue.domain.model

import java.io.File
import java.time.Instant

data class EnqueuedRecognitionWithStatus(
    val id: Int,
    val title: String,
    val recordFile: File,
    val creationDate: Instant,
    val status: EnqueuedRecognitionStatus
)