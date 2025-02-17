package com.mrsep.musicrecognizer.core.domain.recognition

import kotlin.time.Duration

class AudioRecording(
    val data: ByteArray,
    val duration: Duration,
    val nonSilenceDuration: Duration,
)
