package com.mrsep.musicrecognizer.core.recognition.lyrics

import android.annotation.SuppressLint
import com.mrsep.musicrecognizer.core.domain.track.model.SyncedLyrics
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class LyricsConverter {

    private val lineTimeTagPattern = """\[(\d{2}):(\d{2})(?:[.:](\d{2}))?]""".toRegex()
    private val wordTimeTagPattern = """<\d{2}:\d{2}(?:[.:]\d{2})?>""".toRegex()
    private val metadataLinePattern = """^\[([^:]+):(.+)]$""".toRegex()

    fun parseFromString(input: String, parseMetadata: Boolean): SyncedLyricsWithMetadata? {
        val lines = mutableListOf<SyncedLyrics.Line>()
        val metadata = mutableMapOf<String, String>()
        input.lines()
            .mapNotNull { line -> line.trim().takeIf { it.isNotBlank() } }
            .forEach { trimmedLine ->
                val timestamps = mutableListOf<Duration>()
                var currentIndex = 0
                while (true) {
                    val match = lineTimeTagPattern.matchAt(trimmedLine, currentIndex) ?: break
                    val minutes = match.groups[1]!!.value.toInt()
                    val seconds = match.groups[2]!!.value.toInt()
                    val hundredths = match.groups[3]?.value?.toInt() ?: 0
                    val duration = (minutes * 60_000L + seconds * 1_000L + hundredths * 10L).milliseconds
                    timestamps.add(duration)
                    currentIndex = match.range.last + 1
                }
                if (currentIndex != 0 && timestamps.isNotEmpty()) {
                    val content = trimmedLine.substring(currentIndex)
                        .removeEnhancedTimeTags().trim()
                    val taggedLines = timestamps.map { duration ->
                        SyncedLyrics.Line(timestamp = duration, content = content)
                    }
                    lines.addAll(taggedLines)
                } else if (parseMetadata) {
                    val matchResult = metadataLinePattern.matchEntire(trimmedLine) ?: return@forEach
                    val key = matchResult.groups[1]!!.value.trim().lowercase()
                    val value = matchResult.groups[2]!!.value.trim()
                    if (key.isNotEmpty() && value.isNotEmpty()) {
                        metadata[key] = value
                    }
                }
            }
        return lines.takeIf { it.hasMeaningfulContent && it.hasUniqueTimestamps }
            ?.apply { sortBy { it.timestamp } }
            ?.let { SyncedLyricsWithMetadata(metadata, it) }
    }

    // We can add support for enhanced LRC if we find a reliable source; drop words timestamps for now
    private fun String.removeEnhancedTimeTags(): String = when {
        contains(wordTimeTagPattern) -> replace(wordTimeTagPattern, " ")
            .replace("\\s{2,}".toRegex(), " ")
        else -> this
    }

    private val List<SyncedLyrics.Line>.hasMeaningfulContent get() =
        isNotEmpty() && any { it.content.isNotBlank() }

    private val List<SyncedLyrics.Line>.hasUniqueTimestamps get() =
        size == map { it.timestamp }.toSet().size


    fun formatToString(lyrics: SyncedLyricsWithMetadata, formatMetadata: Boolean): String {
        val output = StringBuilder()
        if (formatMetadata) {
            lyrics.metadata.forEach { (tag, value) ->
                output.appendLine("[$tag:$value]")
            }
        }
        lyrics.lines
            .filter { it.timestamp in Duration.ZERO..5_999_990.milliseconds }
            .sortedBy { it.timestamp }
            .groupByTo(LinkedHashMap()) { it.content }
            .forEach { (content, lines) ->
                val timeTags = lines
                    .map { it.timestamp }
                    .sorted()
                    .joinToString("") { it.toLrcTimeTag() }
                output.appendLine("$timeTags$content")
            }
        return output.toString().trim()
    }

    @SuppressLint("DefaultLocale")
    private fun Duration.toLrcTimeTag(): String {
        return toComponents { minutes, seconds, nanoseconds ->
            val hundredths = (nanoseconds / 10_000_000L).toInt()
            String.format("[%02d:%02d.%02d]", minutes, seconds, hundredths)
        }
    }
}

data class SyncedLyricsWithMetadata(
    val metadata: Map<String, String>,
    val lines: List<SyncedLyrics.Line>,
) {
    val title: String? get() = metadata[TAG_TITLE]
    val artist: String? get() = metadata[TAG_ARTIST]
    val album: String? get() = metadata[TAG_ALBUM]
    val author: String? get() = metadata[TAG_AUTHOR]

    val length: Duration? get() = metadata[TAG_LENGTH]
        ?.split(":", limit = 2)
        ?.mapNotNull { it.takeWhile(Char::isDigit).toIntOrNull() }
        ?.takeIf { it.size == 2 }
        ?.let { it[0].minutes + it[1].seconds }

    val offset: Duration get() = metadata[TAG_OFFSET]
        ?.toIntOrNull()?.milliseconds
        ?: Duration.ZERO

    fun copyAndShiftByOffset(shift: Duration): SyncedLyricsWithMetadata? {
        if (shift == Duration.ZERO) return copy()
        if ((lines.first().timestamp + shift).isNegative()) return null
        val newMetadataOffset = (this.offset - shift).inWholeMilliseconds.toString()
        return copy(
            metadata = metadata + (TAG_OFFSET to newMetadataOffset),
            lines = lines.map { it.copy(timestamp = it.timestamp + shift) }
        )
    }

    companion object {
        const val TAG_TITLE = "ti"
        const val TAG_ARTIST = "ar"
        const val TAG_ALBUM = "al"
        const val TAG_AUTHOR = "au"
        const val TAG_LENGTH = "length"
        const val TAG_OFFSET = "offset"
    }
}
