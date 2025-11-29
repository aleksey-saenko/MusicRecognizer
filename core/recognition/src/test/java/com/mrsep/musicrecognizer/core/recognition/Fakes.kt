package com.mrsep.musicrecognizer.core.recognition

import com.mrsep.musicrecognizer.core.domain.recognition.AudioSample
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionProvider
import com.mrsep.musicrecognizer.core.domain.track.model.PlainLyrics
import com.mrsep.musicrecognizer.core.domain.track.model.Track
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import org.junit.rules.TemporaryFolder
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.time.Duration
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
    lyrics = PlainLyrics("lyrics stub"),
    artworkThumbUrl = null,
    artworkUrl = null,
    trackLinks = emptyMap(),
    properties = Track.Properties(
        isFavorite = false,
        isViewed = false,
        themeSeedColor = null,
    ),
)

internal fun emptyAudioRecordingFlow(delayBeforeClose: Long) = flow<AudioSample> {
    delay(delayBeforeClose)
}

internal fun singleAudioRecordingFlow(
    initialDelay: Duration,
    temporaryFolder: TemporaryFolder
) = flow {
    delay(initialDelay)
    emit(
        AudioSample(
            file = File(temporaryFolder.root, "1").also {
                if (!it.exists()) {
                    it.createNewFile()
                    it.writeBytes(ByteArray (10 * 1024) { 1 })
                }
            },
            timestamp = Instant.now(),
            duration = 3.seconds,
            sampleRate = 48_000,
            mimeType = "audio/mp4",
        )
    )
}

internal fun infinityAudioRecordingFlow(
    interval: Duration,
    initialDelay: Duration = interval,
    temporaryFolder: TemporaryFolder
) = flow {
    var counter = 0L
    delay(initialDelay)
    while (true) {
        emit(
            AudioSample(
                file = File(temporaryFolder.root, "1").also {
                    if (!it.exists()) {
                        it.createNewFile()
                        it.writeBytes(ByteArray (10 * 1024) { 1 })
                    }
                },
                timestamp = Instant.now(),
                duration = 3.seconds,
                sampleRate = 48_000,
                mimeType = "audio/mp4",
            )
        )
        println("Emitted rec #$counter")
        counter++
        delay(interval)
    }
}

internal const val normalAudioFlowDuration = 5000L
internal const val normalAudioDelay1 = 0L
internal const val normalAudioDelay2 = 1000L
internal const val normalAudioDelay3 = 1500L
internal const val normalAudioDelay4 = 1000L
internal const val normalAudioDelay5 = 1500L
internal fun normalAudioRecordingFlow(temporaryFolder: TemporaryFolder) = flow {
    delay(normalAudioDelay1)
    emit(
        AudioSample(
            file = File(temporaryFolder.root, "1").also {
                if (!it.exists()) {
                    it.createNewFile()
                    it.writeBytes(ByteArray (10 * 1024) { 1 })
                }
            },
            timestamp = Instant.now(),
            duration = 3.seconds,
            sampleRate = 48_000,
            mimeType = "audio/mp4",
        )
    )
    println("Emitted rec #1")
    delay(normalAudioDelay2)
    emit(
        AudioSample(
            file = File(temporaryFolder.root, "2").also {
                if (!it.exists()) {
                    it.createNewFile()
                    it.writeBytes(ByteArray (10 * 1024) { 2 })
                }
            },
            timestamp = Instant.now(),
            duration = 4.seconds,
            sampleRate = 48_000,
            mimeType = "audio/mp4",
        )
    )
    println("Emitted rec #2")
    delay(normalAudioDelay3)
    emit(
        AudioSample(
            file = File(temporaryFolder.root, "3").also {
                if (!it.exists()) {
                    it.createNewFile()
                    it.writeBytes(ByteArray (10 * 1024) { 3 })
                }
            },
            timestamp = Instant.now(),
            duration = 5.seconds,
            sampleRate = 48_000,
            mimeType = "audio/mp4",
        )
    )
    println("Emitted rec #3")
    delay(normalAudioDelay4)
    emit(
        AudioSample(
            file = File(temporaryFolder.root, "4").also {
                if (!it.exists()) {
                    it.createNewFile()
                    it.writeBytes(ByteArray (10 * 1024) { 4 })
                }
            },
            timestamp = Instant.now(),
            duration = 6.seconds,
            sampleRate = 48_000,
            mimeType = "audio/mp4",
        )
    )
    println("Emitted rec #4")
    delay(normalAudioDelay5)
    emit(
        AudioSample(
            file = File(temporaryFolder.root, "5").also {
                if (!it.exists()) {
                    it.createNewFile()
                    it.writeBytes(ByteArray (10 * 1024) { 5 })
                }
            },
            timestamp = Instant.now(),
            duration = 7.seconds,
            sampleRate = 48_000,
            mimeType = "audio/mp4",
        )
    )
    println("Emitted rec #5")
}
