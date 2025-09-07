package com.mrsep.musicrecognizer.core.audio.audiorecord.encoder

import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

internal data class Interval(
    val startMillis: Long,
    val endMillis: Long,
    val isSilence: Boolean,
)

// This class is NOT thread-safe and must be used within a single thread
internal class UnsafeSilenceTracker(
    initialStartTime: Long = System.currentTimeMillis(),
    initialIsSilence: Boolean = true,
    private val currentTimeProvider: () -> Long = { System.currentTimeMillis() },
) {
    private val intervals = mutableListOf<Interval>()
    private var currentIntervalStart: Long = initialStartTime
    private var currentIsSilence: Boolean = initialIsSilence

    fun onSilenceStateChanged(isSilence: Boolean) {
        val transitionTime = currentTimeProvider()
        intervals.add(
            Interval(
                startMillis = currentIntervalStart,
                endMillis = transitionTime,
                isSilence = currentIsSilence
            )
        )
        currentIntervalStart = transitionTime
        currentIsSilence = isSilence
    }

    fun querySilenceDuration(startTime: Instant, endTime: Instant): Duration {
        val startMillis = startTime.toEpochMilli()
        val endMillis = endTime.toEpochMilli()
        val nowMillis = currentTimeProvider()
        val effectiveEndTime = minOf(endMillis, nowMillis)
        if (startMillis >= effectiveEndTime) return Duration.ZERO

        var totalSilence = 0L
        // Query completed intervals
        if (intervals.isNotEmpty()) {
            val firstIndex = findFirstIntervalIndex(startMillis)
            val lastIndex = findLastIntervalIndex(effectiveEndTime)
            if (firstIndex <= lastIndex) {
                for (i in firstIndex..lastIndex) {
                    val interval = intervals[i]
                    val overlapStart = maxOf(interval.startMillis, startMillis)
                    val overlapEnd = minOf(interval.endMillis, effectiveEndTime)
                    if (overlapStart < overlapEnd && interval.isSilence) {
                        totalSilence += overlapEnd - overlapStart
                    }
                }
            }
        }
        // Query current interval
        val currentOverlapStart = maxOf(currentIntervalStart, startMillis)
        if (currentOverlapStart < effectiveEndTime && currentIsSilence) {
            totalSilence += effectiveEndTime - currentOverlapStart
        }
        return totalSilence.milliseconds
    }

    // Find first interval where endMillis >= startTime argument
    private fun findFirstIntervalIndex(startTime: Long): Int {
        var low = 0
        var high = intervals.lastIndex
        var firstIndex = intervals.size
        while (low <= high) {
            val mid = (low + high) / 2
            if (intervals[mid].endMillis >= startTime) {
                firstIndex = mid
                high = mid - 1
            } else {
                low = mid + 1
            }
        }
        return firstIndex
    }

    // Find last interval where startMillis <= endTime argument
    private fun findLastIntervalIndex(endTime: Long): Int {
        var low = 0
        var high = intervals.lastIndex
        var lastIndex = -1
        while (low <= high) {
            val mid = (low + high) / 2
            if (intervals[mid].startMillis <= endTime) {
                lastIndex = mid
                low = mid + 1
            } else {
                high = mid - 1
            }
        }
        return lastIndex
    }
}
