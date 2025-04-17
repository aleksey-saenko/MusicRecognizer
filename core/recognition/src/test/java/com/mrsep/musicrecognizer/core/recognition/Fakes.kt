package com.mrsep.musicrecognizer.core.recognition

import com.mrsep.musicrecognizer.core.domain.recognition.AudioRecording
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionProvider
import com.mrsep.musicrecognizer.core.domain.track.model.Track
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

internal val fakeTrack = Track(
    id = UUID.randomUUID().toString(),
    title = "Echoes",
    artist = "Pink Floyd",
    album = "Meddle",
    releaseDate = LocalDate.parse("1971-10-30", DateTimeFormatter.ISO_DATE),
    duration = null,
    recognizedAt = null,
    recognizedBy = RecognitionProvider.Audd,
    recognitionDate = Instant.now(),
    lyrics = "lyrics stub",
    artworkThumbUrl = null,
    artworkUrl = null,
    trackLinks = emptyMap(),
    properties = Track.Properties(
        isFavorite = false,
        isViewed = false,
        themeSeedColor = null,
    ),
)

internal fun emptyAudioRecordingFlow(delayBeforeClose: Long) = flow<AudioRecording> {
    delay(delayBeforeClose)
}

internal const val normalAudioFlowDuration = 5000L
internal const val normalAudioDelay1 = 0L
internal const val normalAudioDelay2 = 1000L
internal const val normalAudioDelay3 = 1500L
internal const val normalAudioDelay4 = 1000L
internal const val normalAudioDelay5 = 1500L
internal val normalAudioRecordingFlow get() = flow {
    delay(normalAudioDelay1)
    emit(
        AudioRecording(
            data = ByteArray(100 * 1024),
            duration = 3.seconds,
            nonSilenceDuration = 3.seconds,
            startTimestamp = Instant.now(),
            isFallback = false
        )
    )
    println("Emitted rec #1")
    delay(normalAudioDelay2)
    emit(
        AudioRecording(
            data = ByteArray(200 * 1024),
            duration = 4.seconds,
            nonSilenceDuration = 4.seconds,
            startTimestamp = Instant.now(),
            isFallback = false
        )
    )
    println("Emitted rec #2")
    delay(normalAudioDelay3)
    emit(
        AudioRecording(
            data = ByteArray(300 * 1024),
            duration = 5.seconds,
            nonSilenceDuration = 5.seconds,
            startTimestamp = Instant.now(),
            isFallback = false
        )
    )
    println("Emitted rec #3")
    delay(normalAudioDelay4)
    emit(
        AudioRecording(
            data = ByteArray(400 * 1024),
            duration = 6.seconds,
            nonSilenceDuration = 6.seconds,
            startTimestamp = Instant.now(),
            isFallback = false
        )
    )
    println("Emitted rec #4")
    delay(normalAudioDelay5)
    emit(
        AudioRecording(
            data = ByteArray(500 * 1024),
            duration = 7.seconds,
            nonSilenceDuration = 7.seconds,
            startTimestamp = Instant.now(),
            isFallback = false
        )
    )
    println("Emitted rec #5")
}