package com.mrsep.musicrecognizer.core.audio.audiorecord

import com.mrsep.musicrecognizer.core.audio.audiorecord.encoder.AudioRecordingDataSource
import com.mrsep.musicrecognizer.core.audio.audiorecord.soundsource.SoundSource
import com.mrsep.musicrecognizer.core.domain.recognition.AudioRecordingController
import com.mrsep.musicrecognizer.core.domain.recognition.AudioRecordingSession
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecordingScheme
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.zip
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
internal class DeviceFirstCompositeRecordingController(
    microphoneSoundSource: SoundSource,
    deviceSoundSource: SoundSource,
    defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    audioRecordingDataSource: AudioRecordingDataSource,
) : AudioRecordingController {

    private val microphoneController = CompositeRecordingController(microphoneSoundSource, audioRecordingDataSource)
    private val deviceController = CompositeRecordingController(deviceSoundSource, audioRecordingDataSource)

    override val soundLevel: StateFlow<Float> = combine(
        microphoneSoundSource.soundLevel,
        deviceSoundSource.soundLevel,
    ) { micLevel, devLevel ->
        if (devLevel > 0f) devLevel else micLevel
    }.stateIn(
        CoroutineScope(defaultDispatcher + SupervisorJob()),
        started = SharingStarted.WhileSubscribed(0),
        initialValue = 0f
    )

    context(scope: CoroutineScope)
    override fun startRecordingSession(scheme: RecordingScheme): AudioRecordingSession {
        val microphoneSession = microphoneController.startRecordingSession(scheme)
        val deviceSession = deviceController.startRecordingSession(scheme)

        return object : AudioRecordingSession {
            override val recordings = scope.produce(capacity = Channel.UNLIMITED) {
                try {
                    microphoneSession.recordings.receiveAsFlow()
                        .zip(deviceSession.recordings.receiveAsFlow()) { micRecording, devRecording ->
                            val minSignificantDuration =
                                minOf(2.seconds, micRecording.nonSilenceDuration)
                            if (devRecording.nonSilenceDuration > minSignificantDuration) {
                                devRecording
                            } else {
                                micRecording
                            }
                        }.collect { value ->
                            send(value)
                        }
                } catch (e: Exception) {
                    ensureActive()
                    close(e)
                }
            }

            override suspend fun cancelAndDeleteSessionFiles(): Unit = coroutineScope {
                awaitAll(
                    async { microphoneSession.cancelAndDeleteSessionFiles() },
                    async { deviceSession.cancelAndDeleteSessionFiles() }
                )
            }
        }
    }
}
