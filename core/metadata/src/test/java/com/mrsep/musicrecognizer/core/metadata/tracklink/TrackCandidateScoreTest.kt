package com.mrsep.musicrecognizer.core.metadata.tracklink

import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionProvider
import com.mrsep.musicrecognizer.core.domain.track.model.MusicService
import com.mrsep.musicrecognizer.core.domain.track.model.Track
import org.junit.Test
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Suppress("SpellCheckingInspection")
internal class TrackCandidateScoreTest {

    private val track = Track(
        id = "id",
        title = "Blinding Lights",
        artist = "The Weeknd",
        album = "After Hours",
        duration = 3.minutes + 20.seconds,
        releaseDate = null,
        recognizedAt = null,
        recognizedBy = RecognitionProvider.Shazam,
        recognitionDate = Instant.now(),
        lyrics = null,
        isrc = null,
        artworkThumbUrl = null,
        artworkUrl = null,
        trackLinks = emptyMap(),
        properties = Track.Properties(isFavorite = false, isViewed = false, themeSeedColor = null),
    )

    @Test
    fun printRankingWithDefaultOptions() {
        val candidates = listOf(
            candidate(
                title = "Blinding Lights",
                artist = "The Weeknd",
            ),
            candidate(
                title = "Blinding Light",
                artist = "Weeknd",
            ),
            candidate(
                title = "Blinding Lights (Official Audio)",
                artist = "The Weeknd",
            ),
            candidate(
                title = "Blinding Lights | Official Music Video",
                artist = "The Weeknd",
            ),
            candidate(
                title = "Blinding Lights",
                artist = "Adele",
            ),
            candidate(
                title = "The Weeknd - Blinding Lights",
                artist = "Adele",
            ),
            candidate(
                title = "Blinding Lights <Lyrics>",
                artist = "The Weeknd",
            ),
            candidate(
                title = "Blinding Lights - Live",
                artist = "The Weeknd",
            ),
            candidate(
                title = "Blinding Lights (Cover)",
                artist = "John Doe",
            ),
            candidate(
                title = "Blinding Lights (Instrumental)",
                artist = "The Weeknd",
            ),
            candidate(
                title = "Blinding Lights Remix",
                artist = "The Weeknd",
            ),
            candidate(
                title = "Some Other Song",
                artist = "Another Artist",
            ),
            candidate(
                title = "123",
                artist = "456",
            ),
            candidate(
                title = "1",
                artist = "2",
            ),
        )

        printScores(
            testName = "Default options",
            track = track,
            options = TrackMatcherOptions(),
            candidates = candidates,
        )
    }

    @Test
    fun printRankingConsideringAlbum() {
        val candidates = listOf(
            candidate(),
            candidate(album = "After Hours Deluxe"),
            candidate(album = "The Highlights"),
            candidate(album = "Live at SoFi Stadium"),
            candidate(album = null),
        )

        printScores(
            testName = "Considering album",
            track = track,
            options = TrackMatcherOptions(considerAlbum = true),
            candidates = candidates,
        )
    }

    @Test
    fun printRankingConsideringDuration() {
        val original = candidate()
        val options = TrackMatcherOptions(considerDuration = true)
        val candidates = listOf(
            original,
            candidate(duration = original.duration?.plus(10.seconds)),
            candidate(duration = original.duration?.plus(20.seconds)),
            candidate(duration = original.duration?.plus(30.seconds)),
            candidate(duration = original.duration?.minus(30.seconds)),
            candidate(duration = original.duration?.plus(60.seconds)),
            candidate(duration = original.duration?.plus(120.seconds)),
        )

        printScores(
            testName = "Considering duration, tolerance = ${options.durationTolerance}",
            track = track,
            options = options,
            candidates = candidates,
        )
    }

    @Test
    fun printRankingForCovers() {
        val track = this.track.copy(title = this.track.title + " Cover")
        val track2 = this.track.copy(title = this.track.title + " (Remix)")
        val candidates = listOf(
            candidate(
                title = "Blinding Lights",
                artist = "The Weeknd",
            ),
            candidate(
                title = "Blinding Lights (Cover)",
                artist = "The Weeknd",
            ),
            candidate(
                title = "Blinding Lights [Remix]",
                artist = "The Weeknd",
            ),
        )
        printScores(
            testName = "Cover",
            track = track,
            options = TrackMatcherOptions(),
            candidates = candidates,
        )
        printScores(
            testName = "Remix",
            track = track2,
            options = TrackMatcherOptions(),
            candidates = candidates,
        )
    }

    private fun printScores(
        testName: String,
        track: Track,
        options: TrackMatcherOptions,
        matcher: TrackMatcher = TrackMatcher(track, options),
        candidates: List<TrackCandidate>,
    ) {
        println()
        println("=== $testName ===")
        println("Original: ${track.format(includeAlbum = options.considerAlbum, includeDuration = options.considerDuration)}")
        println("Options: album=${options.considerAlbum}, duration=${options.considerDuration}, tolerance=${options.durationTolerance.inWholeSeconds}s")
        println("Candidates:")
        candidates
            .map { candidate -> candidate to matcher.scoreOf(candidate) }
            .forEach { (candidate, score) ->
                println("${score.formatScore()} | ${candidate.format(includeAlbum = options.considerAlbum, includeDuration = options.considerDuration)}")
            }
    }

    private fun Track.format(
        includeAlbum: Boolean,
        includeDuration: Boolean,
    ): String = buildString {
        append("title=\"")
        append(title)
        append("\", artist=\"")
        append(artist)
        append('"')
        if (includeAlbum) {
            append(", album=")
            append(album?.let { "\"$it\"" } ?: "null")
        }
        if (includeDuration) {
            append(", duration=")
            append(duration?.pretty() ?: "null")
        }
    }

    private fun TrackCandidate.format(
        includeAlbum: Boolean,
        includeDuration: Boolean,
    ): String = buildString {
        append("title=\"")
        append(title)
        append("\", artist=")
        append(artist?.let { "\"$it\"" } ?: "null")
        if (includeAlbum) {
            append(", album=")
            append(album?.let { "\"$it\"" } ?: "null")
        }
        if (includeDuration) {
            append(", duration=")
            append(duration?.pretty() ?: "null")
        }
    }

    private fun Duration.pretty(): String {
        val totalSeconds = inWholeSeconds
        val minutesPart = totalSeconds / 60
        val secondsPart = totalSeconds % 60
        return "%d:%02d".format(minutesPart, secondsPart)
    }

    private fun Double.formatScore(): String = "score=%.2f".format(this)

    private fun candidate(
        title: String = "Blinding Lights",
        artist: String? = "The Weeknd",
        album: String? = "After Hours",
        duration: Duration? = 3.minutes + 20.seconds,
    ): TrackCandidate = TrackCandidate(
        service = MusicService.Youtube,
        title = title,
        artist = artist,
        album = album,
        duration = duration,
        trackUrl = "url",
        artworkThumbUrl = null,
        artworkUrl = null,
    )
}
