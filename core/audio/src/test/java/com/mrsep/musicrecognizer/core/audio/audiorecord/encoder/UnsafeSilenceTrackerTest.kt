package com.mrsep.musicrecognizer.core.audio.audiorecord.encoder

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import java.time.Instant

class UnsafeSilenceTrackerTest {

    private var currentTime = 0L
    private val currentTimeProvider = { currentTime }

    @Before
    fun resetCurrentTime() { currentTime = 0L }

    @Test
    fun `initial silence state`() {
        val tracker = UnsafeSilenceTracker(
            initialStartTime = 0L,
            initialIsSilence = true,
            currentTimeProvider = currentTimeProvider
        )
        currentTime = 500L
        val duration = tracker.querySilenceDuration(
            Instant.ofEpochMilli(0),
            Instant.ofEpochMilli(500)
        )
        assertEquals(500.milliseconds, duration)
    }

    @Test
    fun `initial non-silence state`() {
       val tracker = UnsafeSilenceTracker(
            initialStartTime = 0L,
            initialIsSilence = false,
            currentTimeProvider = currentTimeProvider
        )
        currentTime = 500L
        val duration = tracker.querySilenceDuration(
            Instant.ofEpochMilli(0),
            Instant.ofEpochMilli(500)
        )
        assertEquals(Duration.ZERO, duration)
    }

    @Test
    fun `single state change`() {
        val tracker = UnsafeSilenceTracker(
            initialStartTime = 0L,
            initialIsSilence = true,
            currentTimeProvider = currentTimeProvider
        )
        currentTime = 100L
        tracker.onSilenceStateChanged(false)
        currentTime = 200L
        val duration = tracker.querySilenceDuration(
            Instant.ofEpochMilli(0),
            Instant.ofEpochMilli(200)
        )
        assertEquals(100.milliseconds, duration)
    }

    @Test
    fun `multiple state changes`() {
        val tracker = UnsafeSilenceTracker(
            initialStartTime = 0L,
            initialIsSilence = true,
            currentTimeProvider = currentTimeProvider
        )
        currentTime = 100L
        tracker.onSilenceStateChanged(false)
        currentTime = 200L
        tracker.onSilenceStateChanged(true)
        currentTime = 300L
        tracker.onSilenceStateChanged(false)
        currentTime = 400L
        val duration = tracker.querySilenceDuration(
            Instant.ofEpochMilli(50),
            Instant.ofEpochMilli(350)
        )
        assertEquals(150.milliseconds, duration)
    }

    @Test
    fun `query before all intervals`() {
        val tracker = UnsafeSilenceTracker(
            initialStartTime = 1000L,
            initialIsSilence = true,
            currentTimeProvider = currentTimeProvider
        )
        val duration = tracker.querySilenceDuration(
            Instant.ofEpochMilli(500),
            Instant.ofEpochMilli(900)
        )
        assertEquals(Duration.ZERO, duration)
    }

    @Test
    fun `query after completed intervals`() {
        val tracker = UnsafeSilenceTracker(
            initialStartTime = 0L,
            initialIsSilence = false,
            currentTimeProvider = currentTimeProvider
        )
        currentTime = 100L
        tracker.onSilenceStateChanged(true)
        currentTime = 200L
        val duration = tracker.querySilenceDuration(
            Instant.ofEpochMilli(250),
            Instant.ofEpochMilli(350)
        )
        assertEquals(0.milliseconds, duration)
    }

    @Test
    fun `exact boundary query`() {
        val tracker = UnsafeSilenceTracker(
            initialStartTime = 0L,
            initialIsSilence = true,
            currentTimeProvider = currentTimeProvider
        )
        currentTime = 100L
        tracker.onSilenceStateChanged(false)
        val duration = tracker.querySilenceDuration(
            Instant.ofEpochMilli(100),
            Instant.ofEpochMilli(200)
        )
        assertEquals(Duration.ZERO, duration)
    }

    @Test
    fun `partial overlap with current interval`() {
        val tracker = UnsafeSilenceTracker(
            initialStartTime = 0L,
            initialIsSilence = true,
            currentTimeProvider = currentTimeProvider
        )
        currentTime = 100L
        tracker.onSilenceStateChanged(false)
        currentTime = 200L
        tracker.onSilenceStateChanged(true)
        currentTime = 250L
        val duration = tracker.querySilenceDuration(
            Instant.ofEpochMilli(150),
            Instant.ofEpochMilli(250)
        )
        assertEquals(50.milliseconds, duration)
    }

    @Test
    fun `future end time query`() {
        val tracker = UnsafeSilenceTracker(
            initialStartTime = 0L,
            initialIsSilence = true,
            currentTimeProvider = currentTimeProvider
        )
        currentTime = 100L
        tracker.onSilenceStateChanged(false)
        val duration = tracker.querySilenceDuration(
            Instant.ofEpochMilli(50),
            Instant.ofEpochMilli(150)
        )
        assertEquals(50.milliseconds, duration)
    }

    @Test
    fun `zero duration query`() {
        val tracker = UnsafeSilenceTracker(
            initialStartTime = 0L,
            initialIsSilence = true,
            currentTimeProvider = currentTimeProvider
        )
        val duration = tracker.querySilenceDuration(
            Instant.ofEpochMilli(100),
            Instant.ofEpochMilli(100)
        )
        assertEquals(Duration.ZERO, duration)
    }
}
