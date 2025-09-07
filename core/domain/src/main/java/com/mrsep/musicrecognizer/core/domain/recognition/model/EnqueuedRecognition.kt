package com.mrsep.musicrecognizer.core.domain.recognition.model

import java.io.File
import java.time.Instant

data class EnqueuedRecognition(
    val id: Int,
    val title: String,
    val recordFile: File,
    val creationDate: Instant,
    val result: RemoteRecognitionResult?,
    val resultDate: Instant?
)

enum class ScheduledJobStatus { INACTIVE, ENQUEUED, RUNNING }

data class EnqueuedRecognitionWithStatus(
    val enqueued: EnqueuedRecognition,
    val status: ScheduledJobStatus
)
