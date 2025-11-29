package com.mrsep.musicrecognizer.core.domain.recognition

import java.io.File
import java.time.Instant
import kotlin.time.Duration

data class AudioSample(
    val file: File,
    val timestamp: Instant,
    val duration: Duration,
    val sampleRate: Int,
    val mimeType: String,
)
