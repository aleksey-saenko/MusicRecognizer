package com.mrsep.musicrecognizer.feature.track.presentation.lyrics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

@Stable
internal class LyricsPlayer private constructor(
    private val scope: CoroutineScope,
    private val positionUpdateInterval: Duration = 100.milliseconds
) {
    var isPlaying by mutableStateOf(false)
        private set

    var currentPosition by mutableStateOf(Duration.ZERO)
        private set

    private var positionUpdater: Job? = null
        set(value) {
            field?.cancel()
            field = value
        }

    fun start(startFrom: Duration, trackDuration: Duration) {
        check(startFrom in Duration.ZERO.rangeUntil(trackDuration))
        positionUpdater = scope.launch(Dispatchers.Main) {
            currentPosition = startFrom
            isPlaying = true
            val startTimeMark = TimeSource.Monotonic.markNow()
            while (true) {
                delay(positionUpdateInterval)
                val newPosition = startFrom + startTimeMark.elapsedNow()
                currentPosition = newPosition.coerceIn(Duration.ZERO, trackDuration)
                if (currentPosition >= trackDuration) break
            }
            stop()
        }
    }

    fun stop() {
        positionUpdater = null
        isPlaying = false
        currentPosition = Duration.ZERO
    }

    companion object {

        @Composable
        internal fun rememberLyricsPlayer(
            positionUpdateInterval: Duration = 100.milliseconds,
        ): LyricsPlayer {
            val scope = rememberCoroutineScope()
            return remember(positionUpdateInterval) {
                LyricsPlayer(scope = scope, positionUpdateInterval = positionUpdateInterval)
            }
        }
    }
}
