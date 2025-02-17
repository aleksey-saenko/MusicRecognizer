package com.mrsep.musicrecognizer.core.audio.audiorecord

import android.media.projection.MediaProjection

sealed class AudioCaptureConfig {

    data object Microphone : AudioCaptureConfig()
    data class Device(val mediaProjection: MediaProjection) : AudioCaptureConfig()
    data class Auto(val mediaProjection: MediaProjection) : AudioCaptureConfig()
}
