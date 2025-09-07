package com.mrsep.musicrecognizer.core.audio.audiorecord

import com.mrsep.musicrecognizer.core.audio.audiorecord.encoder.AdtsRecordingController
import com.mrsep.musicrecognizer.core.audio.audiorecord.soundsource.SoundSource
import com.mrsep.musicrecognizer.core.domain.recognition.AudioRecording
import com.mrsep.musicrecognizer.core.domain.recognition.AudioRecordingController
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecordingScheme
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.zip
import kotlin.time.Duration.Companion.seconds

internal class DeviceFirstAdtsRecordingController(
    microphoneSoundSource: SoundSource,
    deviceSoundSource: SoundSource,
    defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : AudioRecordingController {

    private val microphoneController = AdtsRecordingController(microphoneSoundSource)
    private val deviceController = AdtsRecordingController(deviceSoundSource)

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

    override fun audioRecordingFlow(scheme: RecordingScheme): Flow<Result<AudioRecording>> {
        return microphoneController.audioRecordingFlow(scheme)
            .zip(deviceController.audioRecordingFlow(scheme)) { microphoneResult, deviceResult ->
                val micRecording = microphoneResult.getOrThrow()
                val devRecording = deviceResult.getOrThrow()
                val minSignificantDuration = minOf(2.seconds, micRecording.nonSilenceDuration)
                if (devRecording.nonSilenceDuration > minSignificantDuration) {
                    deviceResult
                } else {
                    microphoneResult
                }
            }
            .catch { emit(Result.failure(it)) }
    }
}
