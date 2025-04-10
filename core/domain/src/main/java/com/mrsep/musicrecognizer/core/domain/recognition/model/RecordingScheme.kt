package com.mrsep.musicrecognizer.core.domain.recognition.model

import kotlin.time.Duration

/**
 * Represents a scheme for audio recording.
 *
 * @property steps A list of steps, where each step contains a list of recordings.
 * Each duration in the list starts from the same initial time within the step, and the audio
 * recordings are emitted sequentially based on these durations.
 *
 * The starting point of each step is offset by the total duration of all recordings in the
 * previous steps.
 *
 * @property fallback An optional fallback recording duration, which will be recorded starting
 * from the beginning of the recording process.
 */
data class RecordingScheme(
    val steps: List<Step>,
    val fallback: Duration? = null,
) {
    data class Step(val recordings: List<Duration>)

    init {
        require(steps.isNotEmpty()) { "Steps list must not be empty" }
        steps.forEachIndexed { stepIndex, step ->
            require(step.recordings.isNotEmpty()) {
                "Step at index $stepIndex must contain at least one recording duration"
            }
            step.recordings.forEachIndexed { index, duration ->
                require(duration.isPositive() && duration.isFinite()) {
                    "Duration ($duration) at index $index in step $stepIndex " +
                            "must be positive and finite"
                }
                if (index > 0) {
                    val prevDuration = step.recordings[index - 1]
                    require(duration > prevDuration) {
                        "Duration ($duration) at index $index in step $stepIndex " +
                                "must be greater than the previous one ($prevDuration)"
                    }
                }
            }
        }
    }
}
