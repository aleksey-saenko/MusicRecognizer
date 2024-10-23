package com.mrsep.musicrecognizer.data.audiorecord.soundsource

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface SoundSource {

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
