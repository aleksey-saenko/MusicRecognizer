package com.mrsep.musicrecognizer.core.audio.audiorecord

import android.content.Context
import android.media.projection.MediaProjection
import com.mrsep.musicrecognizer.core.audio.audiorecord.encoder.AudioRecordingDataSource
import com.mrsep.musicrecognizer.core.audio.audiorecord.encoder.Mp4RecordingController
import com.mrsep.musicrecognizer.core.audio.audiorecord.soundsource.DefaultSoundSource
import com.mrsep.musicrecognizer.core.audio.audiorecord.soundsource.VisualizerSoundSource
import com.mrsep.musicrecognizer.core.domain.recognition.AudioRecordingController
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AudioRecordingControllerFactory @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val audioRecordingDataSource: AudioRecordingDataSource
) {

    fun getAudioController(audioCaptureConfig: AudioCaptureConfig): AudioRecordingController {
        fun deviceSoundSource(mediaProjection: MediaProjection?) = if (mediaProjection != null) {
            DefaultSoundSource(appContext, mediaProjection)
        } else {
            VisualizerSoundSource(appContext)
        }
        return when (audioCaptureConfig) {
            AudioCaptureConfig.Microphone -> Mp4RecordingController(
                soundSource = DefaultSoundSource(appContext),
                audioRecordingDataSource = audioRecordingDataSource,
            )

            is AudioCaptureConfig.Device -> Mp4RecordingController(
                soundSource = deviceSoundSource(audioCaptureConfig.mediaProjection),
                audioRecordingDataSource = audioRecordingDataSource,
            )

            is AudioCaptureConfig.Auto -> DeviceFirstAdtsRecordingController(
                microphoneSoundSource = DefaultSoundSource(appContext),
                deviceSoundSource = deviceSoundSource(audioCaptureConfig.mediaProjection),
                audioRecordingDataSource = audioRecordingDataSource,
            )
        }
    }
}
