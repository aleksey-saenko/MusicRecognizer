package com.mrsep.musicrecognizer.core.audio.audiorecord

import android.content.Context
import com.mrsep.musicrecognizer.core.audio.audiorecord.encoder.AdtsRecordingController
import com.mrsep.musicrecognizer.core.audio.audiorecord.soundsource.DefaultSoundSource
import com.mrsep.musicrecognizer.core.audio.audiorecord.soundsource.VisualizerSoundSource
import com.mrsep.musicrecognizer.core.domain.recognition.AudioRecordingController
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AudioRecordingControllerFactory @Inject constructor(
    @ApplicationContext private val appContext: Context,
) {

    fun getAudioController(audioCaptureConfig: AudioCaptureConfig): AudioRecordingController {
        return when (audioCaptureConfig) {
            AudioCaptureConfig.Microphone -> AdtsRecordingController(
                soundSource = DefaultSoundSource(appContext)
            )

            is AudioCaptureConfig.Device -> AdtsRecordingController(
                soundSource = if (audioCaptureConfig.mediaProjection != null) {
                    DefaultSoundSource(appContext, audioCaptureConfig.mediaProjection)
                } else {
                    VisualizerSoundSource(appContext)
                }
            )

            is AudioCaptureConfig.Auto -> DeviceFirstAdtsRecordingController(
                microphoneSoundSource = DefaultSoundSource(appContext),
                deviceSoundSource = if (audioCaptureConfig.mediaProjection != null) {
                    DefaultSoundSource(appContext, audioCaptureConfig.mediaProjection)
                } else {
                    VisualizerSoundSource(appContext)
                }
            )
        }
    }
}
