package com.mrsep.musicrecognizer.feature.recognition.domain.model

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

internal enum class ScheduledJobStatus { INACTIVE, ENQUEUED, RUNNING }

internal data class EnqueuedRecognitionWithStatus(
    val enqueued: EnqueuedRecognition,
    val status: ScheduledJobStatus
)
