package com.mrsep.musicrecognizer.core.audio.audiorecord.prerecording

import kotlin.time.Duration

internal sealed interface PrerecordingSoundSourceState {

    data object Idle : PrerecordingSoundSourceState

    data class Recording(val bufferDuration: Duration?) : PrerecordingSoundSourceState

    data class Closed(val cause: Throwable? = null): PrerecordingSoundSourceState
}
