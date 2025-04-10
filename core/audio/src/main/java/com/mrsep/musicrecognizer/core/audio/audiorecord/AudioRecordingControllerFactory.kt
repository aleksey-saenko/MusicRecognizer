package com.mrsep.musicrecognizer.core.audio.audiorecord

import android.content.Context
import com.mrsep.musicrecognizer.core.audio.audiorecord.encoder.AdtsRecordingController
import com.mrsep.musicrecognizer.core.audio.audiorecord.soundsource.SoundSourceImpl
import com.mrsep.musicrecognizer.core.domain.recognition.AudioRecordingController
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AudioRecordingControllerFactory @Inject constructor(
    @ApplicationContext private val appContext: Context,
) {

    fun getAudioController(audioCaptureConfig: AudioCaptureConfig): AudioRecordingController {
        return when (audioCaptureConfig) {
            AudioCaptureConfig.Microphone -> AdtsRecordingController(
                soundSource = SoundSourceImpl(appContext)
            )

            is AudioCaptureConfig.Device -> AdtsRecordingController(
                soundSource = SoundSourceImpl(appContext, audioCaptureConfig.mediaProjection)
            )

            is AudioCaptureConfig.Auto -> DeviceFirstAdtsRecordingController(
                microphoneSoundSource = SoundSourceImpl(appContext),
                deviceSoundSource = SoundSourceImpl(appContext, audioCaptureConfig.mediaProjection)
            )
        }
    }
}
