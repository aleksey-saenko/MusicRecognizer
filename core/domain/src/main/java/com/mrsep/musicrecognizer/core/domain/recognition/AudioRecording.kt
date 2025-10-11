package com.mrsep.musicrecognizer.core.domain.recognition

import java.io.File
import java.time.Instant
import kotlin.time.Duration

data class AudioRecording(
    val file: File,
    val timestamp: Instant,
    val duration: Duration,
    val nonSilenceDuration: Duration,
    val source: AudioSource,
    val mimeType: String,
    val isFallback: Boolean,
)

enum class AudioSource { MIC, DEVICE }

fun AudioRecording.toAudioSample() = AudioSample(
    file = file,
    timestamp = timestamp,
    duration = duration,
    mimeType = mimeType,
)
