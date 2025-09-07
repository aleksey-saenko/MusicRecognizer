package com.mrsep.musicrecognizer.core.domain.track.model

import kotlin.time.Duration

sealed class Lyrics {
    abstract val plain: String
}

data class PlainLyrics(override val plain: String) : Lyrics()

data class SyncedLyrics(val lines: List<Line>) : Lyrics() {

    override val plain: String by lazy { lines.joinToString("\n") { it.content } }

    data class Line(
        val timestamp: Duration,
        val content: String
    )
}
