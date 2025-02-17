package com.mrsep.musicrecognizer.core.audio.audiorecord

import android.content.Context
import com.mrsep.musicrecognizer.core.audio.audiorecord.soundsource.SoundSourceImpl
import com.mrsep.musicrecognizer.core.domain.recognition.AudioRecordingController
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AudioRecordingControllerFactory @Inject constructor(
    @ApplicationContext private val appContext: Context,
) {

    fun getAudioController(audioCaptureConfig: AudioCaptureConfig): AudioRecordingController {
        val recordingController: AudioRecordingController = when (audioCaptureConfig) {
            AudioCaptureConfig.Microphone -> AudioRecordingControllerImpl(
                soundSource = SoundSourceImpl(appContext)
            )

            is AudioCaptureConfig.Device -> AudioRecordingControllerImpl(
                soundSource = SoundSourceImpl(appContext, audioCaptureConfig.mediaProjection)
            )

            is AudioCaptureConfig.Auto -> DualAudioRecordingControllerImpl(
                microphoneSoundSource = SoundSourceImpl(appContext),
                deviceSoundSource = SoundSourceImpl(appContext, audioCaptureConfig.mediaProjection)
            )
        }

        return recordingController
    }
}