package com.mrsep.musicrecognizer.data.audiorecord

import com.mrsep.musicrecognizer.data.audiorecord.soundsource.SoundSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.zip
import kotlin.time.Duration.Companion.seconds

class DualAudioRecordingControllerImpl(
    microphoneSoundSource: SoundSource,
    deviceSoundSource: SoundSource,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : AudioRecordingControllerDo {

    private val microphoneController = AudioRecordingControllerImpl(microphoneSoundSource)
    private val deviceController = AudioRecordingControllerImpl(deviceSoundSource)

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

    override fun audioRecordingFlow(scheme: RecognitionSchemeDo): Flow<Result<AudioRecordingDo>> {
        return microphoneController.audioRecordingFlow(scheme)
            .zip(deviceController.audioRecordingFlow(scheme)) { microphoneResult, deviceResult ->
                val micRecording = microphoneResult.getOrThrow()
                val devRecording = deviceResult.getOrThrow()
                val minSignificantDuration = minOf(0.5.seconds, micRecording.nonSilenceDuration)
                if (devRecording.nonSilenceDuration > minSignificantDuration) {
                    deviceResult
                } else {
                    microphoneResult
                }
            }
            .catch { emit(Result.failure(it)) }
            .flowOn(defaultDispatcher)
    }
}
