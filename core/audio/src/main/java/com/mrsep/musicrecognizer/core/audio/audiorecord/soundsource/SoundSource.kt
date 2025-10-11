package com.mrsep.musicrecognizer.core.audio.audiorecord.soundsource

import com.mrsep.musicrecognizer.core.domain.recognition.AudioSource
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

internal interface SoundSource {

    val audioSource: AudioSource

    /**
     * Null value represents that there is no available SoundSourceConfig for this device.
     * In this case, audio recording is not allowed.
     */
    val params: SoundSourceConfig?

    /**
     * Represents the current sound level normalized to a range between 0.0 and 1.0.
     * Values are rounded to two decimal places.
     */
    val soundLevel: StateFlow<Float>

    val pcmChunkFlow: SharedFlow<Result<ByteArray>>
}
