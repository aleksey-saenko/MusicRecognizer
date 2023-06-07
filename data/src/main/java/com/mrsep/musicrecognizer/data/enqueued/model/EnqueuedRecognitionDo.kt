package com.mrsep.musicrecognizer.data.enqueued.model

import java.io.File
import java.time.Instant

data class EnqueuedRecognitionDo(
    val id: Int,
    val title: String,
    val recordFile: File,
    val creationDate: Instant,
    val status: EnqueuedRecognitionStatusDo
)