package com.mrsep.musicrecognizer.feature.recognition.presentation.service

import android.content.Intent
import android.media.projection.MediaProjection
import com.mrsep.musicrecognizer.feature.recognition.domain.AudioRecorderController

interface RecognitionControlServiceRouter {

    fun getAudioController(audioCaptureConfig: AudioCaptureConfig): AudioRecorderController

    fun getDeepLinkIntentToTrack(trackId: String): Intent

    fun getDeepLinkIntentToLyrics(trackId: String): Intent
}

sealed class AudioCaptureConfig {
    data object Microphone : AudioCaptureConfig()
    data class Device(val mediaProjection: MediaProjection) : AudioCaptureConfig()
    data class Auto(val mediaProjection: MediaProjection) : AudioCaptureConfig()
}
