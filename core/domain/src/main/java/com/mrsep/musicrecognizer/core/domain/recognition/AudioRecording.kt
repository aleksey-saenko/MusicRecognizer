package com.mrsep.musicrecognizer.core.domain.recognition

import java.time.Instant
import kotlin.time.Duration

class AudioRecording(
    val data: ByteArray,
    val duration: Duration,
    val nonSilenceDuration: Duration,
    val startTimestamp: Instant,
    val isFallback: Boolean,
)
