package com.mrsep.musicrecognizer.domain.model

import java.io.File
import java.time.Instant

data class EnqueuedRecognition(
    val id: Int,
    val title: String,
    val recordFile: File,
    val creationDate: Instant
) {
    val uniqueWorkName get() = "EnqueuedRecognition_id=$id"
}