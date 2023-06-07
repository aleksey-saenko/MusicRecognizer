package com.mrsep.musicrecognizer.data.audiorecord

import kotlin.time.Duration

class AudioRecordingStrategyDo(
    val steps: List<Step>,
    val sendTotalAtEnd: Boolean
) {

    class Step(
        val timestamp: Duration,
        val splitter: Boolean
    )

}