package com.mrsep.musicrecognizer.data.enqueued.model

import java.io.File
import java.time.Instant

data class EnqueuedRecognitionEntityWithStatus(
    val id: Int,
    val title: String,
    val recordFile: File,
    val creationDate: Instant,
    val status: EnqueuedRecognitionDataStatus

)