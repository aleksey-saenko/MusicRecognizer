package com.mrsep.musicrecognizer.core.recognition.enhancer

import android.util.Log
import com.mrsep.musicrecognizer.core.domain.track.model.MusicService
import com.mrsep.musicrecognizer.core.domain.track.model.Track
import info.debatty.java.stringsimilarity.SorensenDice
import java.text.Normalizer
import java.util.Locale
import kotlin.math.abs
import kotlin.math.pow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private const val TAG = "TrackMatcher"

internal data class TrackCandidate(
    val title: String,
    val artist: String?,
    val album: String?,
    val duration: Duration?,
    val artworkThumbUrl: String?,
    val artworkUrl: String?,
    val service: MusicService,
    val trackUrl: String,
)

internal data class ScoredTrackCandidate(
    val candidate: TrackCandidate,
    val score: Double
)

internal data class TrackMatcherOptions(
    val considerAlbum: Boolean = false,
    val considerDuration: Boolean = false,
    val durationTolerance: Duration = 30.seconds,
) {
    init {
        check(!durationTolerance.isNegative())
    }
}

internal class TrackMatcher(
    private val track: Track,
    private val options: TrackMatcherOptions = TrackMatcherOptions(),
    enableCache: Boolean = false,
) {
    private val trackTitleNorm: String = normalize(track.title)
    private val trackArtistNorm: String = normalize(track.artist)
    private val trackAlbumNorm: String = normalize(track.album)

    private val trackTitleTargetsNorm: List<String> = buildTrackTitleTargetsNorm()

    private val trackFullTextNorm: String = combineNormalized(
        trackTitleNorm, trackArtistNorm, trackAlbumNorm,
    )

    private val candidateScoreCache: MutableMap<TrackCandidate, Double>? =
        if (enableCache) mutableMapOf() else null

    fun bestAcceptableOf(
        candidates: Sequence<TrackCandidate>,
        minAcceptScore: Double = 0.70
    ): ScoredTrackCandidate? {
        check(minAcceptScore in 0.0..1.0)
        var best: TrackCandidate? = null
        var bestScore = Double.NEGATIVE_INFINITY
        for (candidate in candidates) {
            val score = scoreOf(candidate)
//            log(candidate, score)
            if (score >= minAcceptScore && score > bestScore) {
                best = candidate
                bestScore = score
            }
        }
        return best?.let { ScoredTrackCandidate(best, bestScore) }
    }

    fun firstAcceptableOf(
        candidates: Sequence<TrackCandidate>,
        minAcceptScore: Double = 0.70
    ): ScoredTrackCandidate? {
        check(minAcceptScore in 0.0..1.0)
        for (candidate in candidates) {
            val score = scoreOf(candidate)
//            log(candidate, score)
            if (score >= minAcceptScore) return ScoredTrackCandidate(candidate, score)
        }
        return null
    }

    fun firstDesiredOrBestAcceptableOf(
        candidates: Sequence<TrackCandidate>,
        minAcceptScore: Double = 0.70,
        desiredAcceptScore: Double
    ): ScoredTrackCandidate? {
        check(minAcceptScore in 0.0..1.0)
        check(desiredAcceptScore in minAcceptScore..1.0)
        var best: TrackCandidate? = null
        var bestScore = Double.NEGATIVE_INFINITY
        for (candidate in candidates) {
            val score = scoreOf(candidate)
//            log(candidate, score)
            if (score >= desiredAcceptScore) return ScoredTrackCandidate(candidate, score)
            if (score >= minAcceptScore && score > bestScore) {
                best = candidate
                bestScore = score
            }
        }
        return best?.let { ScoredTrackCandidate(best, bestScore) }
    }

    fun scoreOf(candidate: TrackCandidate): Double {
        candidateScoreCache?.get(candidate)?.let { return it }
        
        val cleanedCandidateTitle = cleanCandidateTitle(candidate.title)
        
        val titleScore = textMatchScoreNormalized(
            sourceNorm = cleanedCandidateTitle,
            targetsNorm = trackTitleTargetsNorm,
        )

        val artistScore = if (!candidate.artist.isNullOrBlank() && trackArtistNorm.isNotBlank()) {
            var score = textMatchScoreNormalized(
                sourceNorm = normalize(candidate.artist),
                targetsNorm = listOf(trackArtistNorm),
            )
            if (score < 0.3 && trackArtistNorm.isNotBlank() && containsPhrase(cleanedCandidateTitle, trackArtistNorm)) {
                score += 0.3
            }
            score.coerceAtMost(1.0)
        } else {
            null
        }

        val albumScore = if (options.considerAlbum && candidate.album != null && trackAlbumNorm.isNotBlank()) {
            textMatchScoreNormalized(
                sourceNorm = normalize(candidate.album),
                targetsNorm = listOf(trackAlbumNorm),
            )
        } else {
            null
        }

        val components = buildList {
            add(0.60 to titleScore)
            if (artistScore != null) add(0.30 to artistScore)
            if (albumScore != null) add(0.10 to albumScore)
        }

        val weightedSum = components.sumOf { (weight, score) -> weight * score }
        val weightsSum = components.sumOf { it.first }.takeIf { it > 0.0 } ?: 1.0
        val evidence = (weightedSum / weightsSum).coerceIn(0.0, 1.0)

        val candidateText = combineNormalized(
            cleanedCandidateTitle,
            candidate.artist?.let(::normalize),
            if (options.considerAlbum) candidate.album?.let(::normalize) else null,
        )

        val penaltyFactor = penaltyFactor(
            candidateText = candidateText,
            originalText = trackFullTextNorm,
        )

        val trackDuration = track.duration
        val durationFactor = if (options.considerDuration && candidate.duration != null && trackDuration != null) {
            durationSimilarityFactor(
                candidate = candidate.duration,
                expected = trackDuration,
                tolerance = options.durationTolerance,
            )
        } else {
            1.0
        }

        val score = (evidence * penaltyFactor * durationFactor).coerceIn(0.0, 1.0)
        candidateScoreCache?.put(candidate, score)
        return score
    }

    private fun buildTrackTitleTargetsNorm(): List<String> = buildList {
        add(trackTitleNorm)
        add(combineNormalized(trackArtistNorm, trackTitleNorm))
        add(combineNormalized(trackTitleNorm, trackArtistNorm))
        if (trackAlbumNorm.isNotBlank()) {
            add(combineNormalized(trackTitleNorm, trackAlbumNorm))
            add(combineNormalized(trackArtistNorm, trackTitleNorm, trackAlbumNorm))
        }
    }
        .filter { it.isNotBlank() }
        .distinct()

    private fun cleanCandidateTitle(candidateTitle: String): String {
        val source = normalize(candidateTitle)
        if (source.isBlank()) return ""

        var cleaned = source
        for (phrase in NEUTRAL_KEYWORDS) {
            if (containsPhrase(cleaned, phrase) && !containsPhrase(trackFullTextNorm, phrase)) {
                cleaned = removePhrase(cleaned, phrase)
            }
        }

        cleaned = cleaned
            .replace(WHITESPACE_REGEX, " ")
            .trim()

        return if (cleaned.length < 2) source else cleaned
    }

    private fun textMatchScoreNormalized(
        sourceNorm: String,
        targetsNorm: List<String>,
    ): Double {
        if (sourceNorm.isBlank()) return 0.0

        var best = 0.0
        for (target in targetsNorm) {
            if (target.isBlank()) continue
            val s = similarity(sourceNorm, target)
            if (s == 1.0) return 1.0
            best = maxOf(best, s)
        }
        return best
    }

    @Suppress("SpellCheckingInspection")
    private val sorensenDice = SorensenDice(3)
    
    private fun similarity(a: String, b: String): Double {
        if (a.isBlank() || b.isBlank()) return 0.0
        if (a == b) return 1.0
        val k = sorensenDice.k
        if (a.length < k || b.length < k) return 0.0
        return sorensenDice.similarity(a, b)
    }

    private fun durationSimilarityFactor(
        candidate: Duration,
        expected: Duration,
        tolerance: Duration,
    ): Double {
        if (tolerance == Duration.ZERO && candidate != expected) return 0.0
        val diffMs = abs(candidate.inWholeMilliseconds - expected.inWholeMilliseconds).toDouble()
        val toleranceMs = tolerance.inWholeMilliseconds.toDouble().coerceAtLeast(1.0)

        if (diffMs <= toleranceMs) return 1.0

        val excessMs = diffMs - toleranceMs
        val ratio = excessMs / toleranceMs
        return (1.0 / (1.0 + (ratio / 2.0).pow(2))).coerceIn(0.0, 1.0)
    }

    private fun penaltyFactor(
        candidateText: String,
        originalText: String,
    ): Double {
        var penalty = 0.0
        for ((phrase, weight) in NEGATIVE_KEYWORDS) {
            if (containsPhrase(candidateText, phrase) && !containsPhrase(originalText, phrase)) {
                penalty += weight
            }
        }
        return (1.0 / (1.0 + penalty)).coerceIn(0.0, 1.0)
    }

    companion object {

        private val NEGATIVE_KEYWORDS: List<Pair<String, Double>> = listOf(
            "remix" to 0.70,
            "cover" to 0.80,
            "karaoke" to 0.70,
            "instrumental" to 0.90,
            "acapella" to 0.70,
            "live" to 0.40,
            "sped up" to 1.00,
            "slowed" to 1.00,
            "nightcore" to 1.00,
            "reverb" to 0.45,
            "mashup" to 1.00,
            "tribute" to 0.60,
            "reaction" to 1.20,
            "tutorial" to 1.20,
            "commentary" to 1.20,
            "parody" to 1.20,
            "review" to 1.20,
            "performance" to 0.45,
            "demo" to 0.35,
            "extended" to 0.10,
            "loop" to 0.50,
            "8d" to 0.50,
            "fan" to 0.80,
        )

        private val NEUTRAL_KEYWORDS: List<String> = listOf(
            "official music video",
            "official music",
            "official video",
            "official audio",
            "official lyric",
            "music video",
            "lyric video",
            "lyrics video",
            "high quality",
            "audio",
            "video",
            "music",
            "lyric",
            "lyrics",
            "hq",
            "hd",
            "4k",
            "topic",
            "studio",
            "vevo",
            "explicit",
        )

        private val NON_ALNUM_REGEX = Regex("[^\\p{L}\\p{Nd}]+")
        private val WHITESPACE_REGEX = Regex("\\s+")
        private val DIACRITICS_REGEX = Regex("\\p{Mn}+")

        private fun containsPhrase(text: String, phrase: String): Boolean {
            if (text.isBlank() || phrase.isBlank()) return false
            return " $text ".contains(" $phrase ")
        }

        private fun removePhrase(text: String, phrase: String): String {
            if (text.isBlank() || phrase.isBlank()) return text
            return " $text ".replace(" $phrase ", " ").trim()
        }

        private fun normalize(value: String?): String {
            if (value.isNullOrBlank()) return ""

            val decomposed = Normalizer.normalize(value, Normalizer.Form.NFD)
            val withoutDiacritics = decomposed.replace(DIACRITICS_REGEX, "")

            return withoutDiacritics
                .lowercase(Locale.ROOT)
                .replace(NON_ALNUM_REGEX, " ")
                .replace(WHITESPACE_REGEX, " ")
                .trim()
        }

        private fun combineNormalized(vararg parts: String?): String {
            return parts.filter { !it.isNullOrBlank() }.joinToString(" ")
        }

        private fun log(candidate: TrackCandidate, score: Double) {
            Log.d(TAG, "${candidate.service} | %.2f | ".format(score) + candidate.toString())
        }
    }
}
