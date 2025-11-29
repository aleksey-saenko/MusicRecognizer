package com.mrsep.musicrecognizer.core.recognition.shazam

import com.github.f4b6a3.uuid.UuidCreator
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionProvider
import com.mrsep.musicrecognizer.core.domain.recognition.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.core.domain.track.model.MusicService
import com.mrsep.musicrecognizer.core.domain.track.model.PlainLyrics
import com.mrsep.musicrecognizer.core.domain.track.model.Track
import java.time.Instant
import java.time.LocalDate
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal fun ShazamResponseJson.toRecognitionResult(
    sampleStartTimestamp: Instant,
    sampleDuration: Duration,
): RemoteRecognitionResult {
    if (this.track == null ||
        track.title.isNullOrBlank() ||
        track.subtitle.isNullOrBlank()
    ) return RemoteRecognitionResult.NoMatches
    require(track.key?.isNotEmpty() == true)
    return RemoteRecognitionResult.Success(
        track = Track(
            id = createTrackId(track.key),
            title = track.title,
            artist = track.subtitle,
            album = track.sections
                ?.firstOrNull { section -> section?.type?.uppercase() == "SONG" }
                ?.metadata
                ?.firstOrNull { metadata -> metadata?.title?.uppercase() == "ALBUM" }
                ?.text,
            releaseDate = track.sections
                ?.firstOrNull { section -> section?.type?.uppercase() == "SONG" }
                ?.metadata
                ?.firstOrNull { metadata -> metadata?.title?.uppercase() == "RELEASED" }
                ?.text?.toIntOrNull()?.run { LocalDate.ofYearDay(this, 1) }, // fixme: It's not a correct release date
            duration = null,
            recognizedAt = matches?.firstOrNull()?.offset?.seconds,
            recognizedBy = RecognitionProvider.Shazam,
            recognitionDate = sampleStartTimestamp,
            lyrics = track.sections
                ?.firstOrNull { section -> section?.type?.uppercase() == "LYRICS" }
                ?.text
                ?.joinToString("\n")
                ?.run(::PlainLyrics),
            trackLinks = run {
                val result = mutableMapOf<MusicService, String>()
                track?.hub
                    ?.options
                    ?.find { it?.type?.uppercase() == "OPEN" && it.providername?.uppercase() == "APPLEMUSIC" }
                    ?.actions?.find { it?.name == "hub:applemusic:deeplink" }
                    ?.uri?.replaceBefore("music.apple.com", "https://")
                    ?.replaceAfter("&", "")?.dropLast(1)
                    ?.run { result.put(MusicService.AppleMusic, this) }
                result
            },
            artworkThumbUrl = this.track.images?.coverarthq?.convertImageUrl("400x400cc")
                ?: this.track.images?.background?.convertImageUrl("400x400cc")
                ?: this.track.images?.coverart?.convertImageUrl("400x400cc"),
            artworkUrl = this.track.images?.coverarthq?.convertImageUrl("1500x1500cc")
                ?: this.track.images?.background?.convertImageUrl("1500x1500cc")
                ?: this.track.images?.coverart?.convertImageUrl("1500x1500cc"),
            properties = Track.Properties(
                isFavorite = false,
                isViewed = false,
                themeSeedColor = null
            )
        )
    )
}

private fun String.convertImageUrl(requiredSize: String): String? {
    val pattern = Regex("\\d+x\\d+cc")
    return if (pattern.containsMatchIn(this)) {
        pattern.replace(this, requiredSize)
    } else {
        null
    }
}

private fun createTrackId(shazamTrackKey: String): String {
    // Define some namespace for Shazam to distinguish result UUIDs in cases
    // when tracks recognized by different providers have the same remote ID
    val acrCloudIdNamespace = "35f0e68f-421e-40ce-847e-f5b3c5ab77e5"
    return UuidCreator.getNameBasedSha1(acrCloudIdNamespace, shazamTrackKey).toString()
}