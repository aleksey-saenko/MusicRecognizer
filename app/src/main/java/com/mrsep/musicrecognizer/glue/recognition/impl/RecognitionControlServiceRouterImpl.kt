package com.mrsep.musicrecognizer.glue.recognition.impl

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import androidx.core.net.toUri
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.audiorecord.RecognitionSchemeDo
import com.mrsep.musicrecognizer.data.audiorecord.DualAudioRecordingControllerImpl
import com.mrsep.musicrecognizer.data.audiorecord.AudioRecordingControllerImpl
import com.mrsep.musicrecognizer.data.audiorecord.AudioRecordingControllerDo
import com.mrsep.musicrecognizer.data.audiorecord.soundsource.SoundSourceImpl
import com.mrsep.musicrecognizer.feature.recognition.domain.AudioRecorderController
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionScheme
import com.mrsep.musicrecognizer.feature.recognition.presentation.queuescreen.RecognitionQueueScreen
import com.mrsep.musicrecognizer.feature.recognition.presentation.service.AudioCaptureConfig
import com.mrsep.musicrecognizer.feature.recognition.presentation.service.RecognitionControlServiceRouter
import com.mrsep.musicrecognizer.feature.track.presentation.lyrics.LyricsScreen
import com.mrsep.musicrecognizer.feature.track.presentation.track.TrackScreen
import com.mrsep.musicrecognizer.presentation.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RecognitionControlServiceRouterImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val recognitionSchemeMapper: Mapper<RecognitionScheme, RecognitionSchemeDo>,
) : RecognitionControlServiceRouter {

    override fun getAudioController(audioCaptureConfig: AudioCaptureConfig): AudioRecorderController {
        val recordingControllerDo: AudioRecordingControllerDo = when (audioCaptureConfig) {
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

        return object : AudioRecorderController {
            override val soundLevel: Flow<Float> = recordingControllerDo.soundLevel

            override suspend fun audioRecordingFlow(scheme: RecognitionScheme): Flow<Result<ByteArray>> {
                return recordingControllerDo
                    .audioRecordingFlow(recognitionSchemeMapper.map(scheme))
                    .map { result -> result.map { recording -> recording.data } }
            }
        }
    }

    override fun getDeepLinkIntentToTrack(trackId: String): Intent {
        return getDeepLinkIntent(TrackScreen.createDeepLink(trackId).toUri())
    }

    override fun getDeepLinkIntentToLyrics(trackId: String): Intent {
        return getDeepLinkIntent(LyricsScreen.createDeepLink(trackId).toUri())
    }

    override fun getDeepLinkIntentToQueue(): Intent {
        return getDeepLinkIntent(RecognitionQueueScreen.createDeepLink().toUri())
    }

    private fun getDeepLinkIntent(uri: Uri): Intent {
        return Intent(
            Intent.ACTION_VIEW,
            uri,
            appContext,
            MainActivity::class.java
        ).apply {
            addFlags(FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
