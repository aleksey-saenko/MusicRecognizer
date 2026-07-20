package com.mrsep.musicrecognizer.core.domain.track.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

sealed class Lyrics {
    abstract val plain: String
}

data class PlainLyrics(override val plain: String) : Lyrics()

data class SyncedLyrics(val lines: List<Line>) : Lyrics() {

    init {
        check(lines.isNotEmpty()) { "Synced lyrics must contain at least one line" }
    }

    override val plain: String by lazy { lines.joinToString("\n") { it.content } }

    data class Line(
        val timestamp: Duration,
        val content: String
    )

    // The first line must be empty or start from zero, otherwise we can get null for early positions
    fun currentLineIndex(currentOffset: Duration): Int? {
        if (lines.isEmpty()) return null
        if (currentOffset < lines.first().timestamp) return null
        if (currentOffset >= lines.last().timestamp) return lines.lastIndex
        return lines.binarySearch { line ->
            line.timestamp.compareTo(currentOffset)
        }.let { result ->
            // Exact match or (insertion point - 1)
            if (result >= 0) result else -(result + 1) - 1
        }.takeIf { it >= 0 }
    }

    fun hasMeaningfulRemainingDuration(
        currentLineIndex: Int,
        trackDuration: Duration,
        threshold: Duration = 3.seconds,
    ): Boolean {
        val lastMeaningfulLineIndex = lines.lastIndex - lines.asReversed().indexOfFirst { it.content.isNotBlank() }
        if (lastMeaningfulLineIndex < 0) return false

        var meaningfulRemainingDuration = Duration.ZERO
        for (index in currentLineIndex..lastMeaningfulLineIndex) {
            val nextLineTimestamp = lines.getOrNull(index + 1)?.timestamp ?: trackDuration
            val thisLineDuration = nextLineTimestamp - lines.get(index).timestamp
            meaningfulRemainingDuration += thisLineDuration
            if (meaningfulRemainingDuration > threshold) return true
        }
        return false
    }
}
