package com.mrsep.musicrecognizer.data.audiorecord

import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

interface AudioRecordingController {

    fun audioRecordingFlow(strategy: AudioRecordingDataStrategy): Flow<Result<ByteArray>>

}

class AudioRecordingDataStrategy(
    val steps: List<Step>,
    val sendTotalAtEnd: Boolean
) {

    class Step(
        val timestamp: Duration,
        val splitter: Boolean
    )

}
