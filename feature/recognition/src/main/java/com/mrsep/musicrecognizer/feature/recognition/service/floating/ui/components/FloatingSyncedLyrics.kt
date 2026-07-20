package com.mrsep.musicrecognizer.feature.recognition.service.floating.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.LifecycleStartEffect
import com.mrsep.musicrecognizer.core.domain.track.model.SyncedLyrics
import com.mrsep.musicrecognizer.core.ui.components.LyricsPlayer.Companion.rememberLyricsPlayer
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toKotlinDuration

private const val ScrollAnimationDurationMs = 350
private const val FadeAnimationDurationMs = 200
private val ScrollAnimationEasing = FastOutSlowInEasing

@Composable
internal fun FloatingSyncedLyrics(
    modifier: Modifier = Modifier,
    lyrics: SyncedLyrics,
    trackDuration: Duration?,
    recognizedAt: Duration?,
    recognitionDate: Instant,
    onPlaybackStarted: () -> Unit = {},
    onPlaybackStopped: () -> Unit = {},
    verticalPadding: Dp,
) {
    check(lyrics.lines.firstOrNull()?.timestamp == Duration.ZERO)
    val lines = lyrics.lines

    val actualTrackDuration = remember(trackDuration, lines) {
        val minLyricsDuration = (lines.lastOrNull()?.timestamp ?: Duration.ZERO) + 2.seconds
        trackDuration?.coerceAtLeast(minLyricsDuration) ?: minLyricsDuration
    }

    val lyricsPlayer = rememberLyricsPlayer()
    val currentOnPlaybackStarted by rememberUpdatedState(onPlaybackStarted)
    val currentOnPlaybackStopped by rememberUpdatedState(onPlaybackStopped)

    LifecycleStartEffect(recognizedAt, recognitionDate, actualTrackDuration) {
        if (!lyricsPlayer.isPlaying && recognizedAt != null && lines.isNotEmpty()) {
            val timePassed = java.time.Duration.between(recognitionDate, Instant.now()).toKotlinDuration()
            val currentPlaybackOffset = recognizedAt + timePassed
            if (currentPlaybackOffset in Duration.ZERO.rangeUntil(actualTrackDuration)) {
                lyricsPlayer.start(currentPlaybackOffset, actualTrackDuration)
                currentOnPlaybackStarted()
            }
        }
        onStopOrDispose {
            lyricsPlayer.stop()
            currentOnPlaybackStopped()
        }
    }

    LaunchedEffect(lyricsPlayer) {
        var wasPlaying = false
        snapshotFlow { lyricsPlayer.isPlaying }.collect { isPlaying ->
            if (isPlaying && !wasPlaying) {
                currentOnPlaybackStarted()
                wasPlaying = true
            } else if (!isPlaying && wasPlaying) {
                currentOnPlaybackStopped()
                wasPlaying = false
            }
        }
    }

    val currentLineState by remember(lyrics) {
        derivedStateOf {
            if (!lyricsPlayer.isPlaying || lines.isEmpty()) return@derivedStateOf null

            val position = lyricsPlayer.currentPosition
            val strictIndex = lyrics.currentLineIndex(position) ?: return@derivedStateOf null

            // Shift active line to compensate for scroll and highlight animation duration
            // Capped to half of the previous line's duration to prevent overlaps
            var displayIndex = strictIndex
            if (strictIndex < lines.lastIndex) {
                val currentLine = lines[strictIndex]
                val nextLine = lines[strictIndex + 1]
                val lineDuration = nextLine.timestamp - currentLine.timestamp
                val lookAheadTime = minOf(ScrollAnimationDurationMs.milliseconds, lineDuration / 2)
                if (position + lookAheadTime >= nextLine.timestamp) {
                    displayIndex = strictIndex + 1
                }
            }

            lines[displayIndex] to displayIndex
        }
    }

    val textStyle = MaterialTheme.typography.titleMedium
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    val threeLinesHeightDp = remember(textStyle, density) {
        val heightPx = textMeasurer.measure(
            text = "H\nH\nH",
            style = textStyle
        ).size.height
        with(density) { heightPx.toDp() }
    }

    AnimatedContent(
        modifier = modifier
            .height(threeLinesHeightDp + verticalPadding * 2),
        contentAlignment = Alignment.Center,
        targetState = currentLineState,
        transitionSpec = lyricsLineTransform,
    ) { state ->
        if (state == null) {
            Box(contentAlignment = Alignment.Center) { Text("") }
        } else {
            val (line, index) = state

            if (line.content.isNotBlank()) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = line.content,
                        maxLines = 3,
                        style = textStyle,
                        color = LocalContentColor.current,
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            } else {
                val nextTimestamp = remember(index) {
                    lines.getOrNull(index + 1)?.timestamp ?: actualTrackDuration
                }
                Box(contentAlignment = Alignment.Center) {
                    BubblesLine(
                        time = line.timestamp,
                        nextTime = nextTimestamp,
                        textStyle = textStyle,
                        lyricsPlayer = lyricsPlayer
                    )
                }
            }
        }
    }
}

internal fun shouldShowFloatingLyrics(
    lyrics: SyncedLyrics,
    trackDuration: Duration?,
    recognizedAt: Duration?,
    recognitionDate: Instant,
    threshold: Duration,
): Boolean {
    if (lyrics.lines.isEmpty() || recognizedAt == null) return false

    val minLyricsDuration = lyrics.lines.last().timestamp + 2.seconds
    val actualTrackDuration = trackDuration?.coerceAtLeast(minLyricsDuration) ?: minLyricsDuration

    val timePassed = java.time.Duration.between(recognitionDate, Instant.now()).toKotlinDuration()
    val currentPlaybackOffset = recognizedAt + timePassed

    if (currentPlaybackOffset !in Duration.ZERO.rangeUntil(actualTrackDuration)) return false

    val currentLineIndex = lyrics.currentLineIndex(currentPlaybackOffset) ?: return false
    return lyrics.hasMeaningfulRemainingDuration(
        currentLineIndex,
        actualTrackDuration,
        threshold = threshold
    )
}

private val lyricsLineTransform: AnimatedContentTransitionScope<Pair<SyncedLyrics.Line, Int>?>.() -> ContentTransform = {
    slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Up,
        animationSpec = tween(
            durationMillis = ScrollAnimationDurationMs,
            easing = ScrollAnimationEasing
        ),
        initialOffset = { it }
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = FadeAnimationDurationMs,
            easing = ScrollAnimationEasing
        )
    ) togetherWith slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Up,
        animationSpec = tween(
            durationMillis = ScrollAnimationDurationMs,
            easing = ScrollAnimationEasing
        ),
        targetOffset = { it }
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = FadeAnimationDurationMs,
            easing = ScrollAnimationEasing
        )
    )
}
