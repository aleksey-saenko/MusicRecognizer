package com.mrsep.musicrecognizer.core.audio.audiorecord

import android.content.Context
import android.media.projection.MediaProjection
import com.mrsep.musicrecognizer.core.audio.audiorecord.encoder.AudioRecordingDataSource
import com.mrsep.musicrecognizer.core.audio.audiorecord.prerecording.PrerecordingSoundSource
import com.mrsep.musicrecognizer.core.audio.audiorecord.prerecording.SoundSourceRegistry
import com.mrsep.musicrecognizer.core.audio.audiorecord.prerecording.SoundSourceKey
import com.mrsep.musicrecognizer.core.audio.audiorecord.soundsource.DefaultSoundSource
import com.mrsep.musicrecognizer.core.audio.audiorecord.soundsource.SoundSource
import com.mrsep.musicrecognizer.core.audio.audiorecord.soundsource.VisualizerSoundSource
import com.mrsep.musicrecognizer.core.domain.recognition.AudioRecordingController
import com.mrsep.musicrecognizer.core.domain.recognition.AudioRecordingSession
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecordingScheme
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.time.Duration

interface AudioRecordingControllerFactory {
    suspend fun getAudioController(audioCaptureConfig: AudioCaptureConfig): AudioRecordingController
}

internal class AudioRecordingControllerFactoryImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val audioRecordingDataSource: AudioRecordingDataSource,
    private val soundSourceRegistry: SoundSourceRegistry,
) : AudioRecordingControllerFactory {

    override suspend fun getAudioController(audioCaptureConfig: AudioCaptureConfig): AudioRecordingController {
        return when (audioCaptureConfig) {

            AudioCaptureConfig.Microphone -> {
                val micSourceDescriptor = microphoneDescriptor(appContext)
                val micSoundSource = prerecordingSoundSource(micSourceDescriptor)

                val sessionFactory = CompositeRecordingController(
                    soundSource = micSoundSource,
                    audioRecordingDataSource = audioRecordingDataSource,
                )

                object : AudioRecordingController {
                    override val soundLevel = micSoundSource.soundLevel

                    context(scope: CoroutineScope)
                    override fun startRecordingSession(scheme: RecordingScheme, includeBuffered: Boolean): AudioRecordingSession {
                        return sessionFactory.startRecordingSession(scheme, includeBuffered)
                    }

                    override suspend fun startPrerecording(bufferDuration: Duration) {
                        micSoundSource.startPrerecording(bufferDuration)
                    }

                    override suspend fun stopPrerecording() {
                        micSoundSource.stopPrerecording()
                    }

                    override suspend fun release() {
                        soundSourceRegistry.close(micSourceDescriptor.key)
                    }
                }
            }

            is AudioCaptureConfig.Device -> {
                val deviceSourceDescriptor = deviceDescriptor(
                    appContext,
                    audioCaptureConfig.mediaProjection,
                    audioCaptureConfig.mediaProjectionId
                )
                val deviceSoundSource = prerecordingSoundSource(deviceSourceDescriptor)

                val sessionFactory = CompositeRecordingController(
                    soundSource = deviceSoundSource,
                    audioRecordingDataSource = audioRecordingDataSource,
                )

                object : AudioRecordingController {
                    override val soundLevel = deviceSoundSource.soundLevel

                    context(scope: CoroutineScope)
                    override fun startRecordingSession(scheme: RecordingScheme, includeBuffered: Boolean): AudioRecordingSession {
                        return sessionFactory.startRecordingSession(scheme, includeBuffered)
                    }

                    override suspend fun startPrerecording(bufferDuration: Duration) {
                        deviceSoundSource.startPrerecording(bufferDuration)
                    }

                    override suspend fun stopPrerecording() {
                        deviceSoundSource.stopPrerecording()
                    }

                    override suspend fun release() {
                        soundSourceRegistry.close(deviceSourceDescriptor.key)
                    }
                }
            }

            is AudioCaptureConfig.Auto -> {
                val micSourceDescriptor = microphoneDescriptor(appContext)
                val micSoundSource = prerecordingSoundSource(micSourceDescriptor)

                val deviceSourceDescriptor = deviceDescriptor(
                    appContext,
                    audioCaptureConfig.mediaProjection,
                    audioCaptureConfig.mediaProjectionId
                )
                val deviceSoundSource = prerecordingSoundSource(deviceSourceDescriptor)

                val sessionFactory = DeviceFirstCompositeRecordingController(
                    microphoneSoundSource = micSoundSource,
                    deviceSoundSource = deviceSoundSource,
                    audioRecordingDataSource = audioRecordingDataSource,
                )

                object : AudioRecordingController {
                    private val soundLevelScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
                    override val soundLevel: StateFlow<Float> = combine(
                        micSoundSource.soundLevel,
                        deviceSoundSource.soundLevel,
                    ) { micLevel, devLevel ->
                        if (devLevel > 0f) devLevel else micLevel
                    }.stateIn(
                        soundLevelScope,
                        started = SharingStarted.WhileSubscribed(0),
                        initialValue = 0f
                    )

                    context(scope: CoroutineScope)
                    override fun startRecordingSession(scheme: RecordingScheme, includeBuffered: Boolean): AudioRecordingSession {
                        return sessionFactory.startRecordingSession(scheme, includeBuffered)
                    }

                    override suspend fun startPrerecording(bufferDuration: Duration) {
                        micSoundSource.startPrerecording(bufferDuration)
                        deviceSoundSource.startPrerecording(bufferDuration)
                    }

                    override suspend fun stopPrerecording() {
                        micSoundSource.stopPrerecording()
                        deviceSoundSource.stopPrerecording()
                    }

                    override suspend fun release() {
                        soundLevelScope.cancel()
                        soundSourceRegistry.close(micSourceDescriptor.key)
                        soundSourceRegistry.close(deviceSourceDescriptor.key)
                    }
                }
            }
        }
    }

    private suspend fun prerecordingSoundSource(descriptor: SourceDescriptor): PrerecordingSoundSource {
        return soundSourceRegistry.getOrCreate(descriptor.key, descriptor.create)
    }

    private fun microphoneDescriptor(appContext: Context) = SourceDescriptor(
        key = SoundSourceKey.Microphone(),
        create = { DefaultSoundSource(appContext) },
    )

    private fun deviceDescriptor(
        appContext: Context,
        mediaProjection: MediaProjection?,
        mediaProjectionId: String?,
    ) = SourceDescriptor(
        key = SoundSourceKey.DeviceOutput(projectionTokenId = mediaProjectionId ?: "visualizer"),
        create = {
            if (mediaProjection != null) {
                DefaultSoundSource(appContext, mediaProjection)
            } else {
                VisualizerSoundSource(appContext)
            }
        },
    )

    private data class SourceDescriptor(
        val key: SoundSourceKey,
        val create: () -> SoundSource,
    )
}
