package com.mrsep.musicrecognizer.core.recognition.audd.json

import android.text.Html
import android.util.Log
import androidx.core.graphics.toColorInt
import com.github.f4b6a3.uuid.UuidCreator
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionProvider
import com.mrsep.musicrecognizer.core.domain.recognition.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.core.domain.track.model.MusicService
import com.mrsep.musicrecognizer.core.domain.track.model.PlainLyrics
import com.mrsep.musicrecognizer.core.domain.track.model.Track
import com.mrsep.musicrecognizer.core.recognition.artwork.TrackArtwork
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private const val TAG = "AuddResponseJsonMapper"

internal fun AuddResponseJson.toRecognitionResult(
    json: Json,
    recordingStartTimestamp: Instant,
    recordingDuration: Duration,
): RemoteRecognitionResult {
    return when (status) {
        "success" -> if (result == null) {
            RemoteRecognitionResult.NoMatches
        } else {
            parseSuccessResult(result, recordingStartTimestamp, recordingDuration, json)
        }

        "error" -> if (error == null) {
            getUnknownError()
        } else {
            parseErrorResult(error)
        }

        else -> getUnknownError()
    }
}

private fun parseSuccessResult(
    result: AuddResponseJson.Result,
    recordingStartTimestamp: Instant,
    recordingDuration: Duration,
    json: Json,
): RemoteRecognitionResult {
    val trackTitle = result.parseTrackTitle()
    val trackArtist = result.parseTrackArtist()
    if (trackTitle.isNullOrBlank() || trackArtist.isNullOrBlank()) {
        return RemoteRecognitionResult.NoMatches
    }
    val mediaItems = result.lyricsJson?.parseMediaItems(json)
    val trackArtwork = result.toTrackArtwork()

    val trackDuration = result.parseTrackDuration()
    val recognizedAt = result.timecode?.run(::parseRecognizedAt)
        ?.minus(recordingDuration)
        ?.coerceIn(Duration.ZERO, trackDuration)

    return RemoteRecognitionResult.Success(
        track = Track(
            id = createTrackId(result),
            title = trackTitle,
            artist = trackArtist,
            album = result.parseAlbum(),
            releaseDate = result.parseReleaseDate(),
            duration = trackDuration,
            recognizedAt = recognizedAt,
            recognizedBy = RecognitionProvider.Audd,
            recognitionDate = recordingStartTimestamp,
            lyrics = result.parseLyrics(),
            artworkUrl = trackArtwork?.url,
            artworkThumbUrl = trackArtwork?.thumbUrl,
            trackLinks = buildMap {
                result.parseAppleMusicLink()?.run { put(MusicService.AppleMusic, this) }
                result.parseDeezerLink()?.run { put(MusicService.Deezer, this) }
                result.parseMusicBrainzLink()?.run { put(MusicService.MusicBrainz, this) }
                result.parseNapsterLink()?.run { put(MusicService.Napster, this) }
                mediaItems?.parseSoundCloudLink()?.run { put(MusicService.Soundcloud, this) }
                result.parseSpotifyLink()?.run { put(MusicService.Spotify, this) }
                mediaItems?.parseYoutubeLink()?.run { put(MusicService.Youtube, this) }
            },
            properties = Track.Properties(
                isFavorite = false,
                isViewed = false,
                themeSeedColor = null,
            ),
        )
    )
}

private fun createTrackId(result: AuddResponseJson.Result): String {
    return result.auddSongLink?.let {
        // Define some namespace for Audd to distinguish result UUIDs in cases
        // when tracks recognized by different providers have the same remote ID
        val auddIdNamespace = "bbfa96d0-dae8-4273-912a-0d9e2e1931c2"
        UuidCreator.getNameBasedSha1(auddIdNamespace, result.auddSongLink).toString()
    } ?: UUID.randomUUID().toString()
}

private fun AuddResponseJson.Result.parseTrackTitle(): String? {
    return appleMusic?.name?.takeIf { it.isNotBlank() }
        ?: deezerJson?.title?.takeIf { it.isNotBlank() }
        ?: title?.takeIf { it.isNotBlank() }
        ?: spotify?.name?.takeIf { it.isNotBlank() }
        ?: napster?.name?.takeIf { it.isNotBlank() }
        ?: musicbrainz?.firstOrNull()?.title?.takeIf { it.isNotBlank() }
}

private fun AuddResponseJson.Result.parseTrackArtist(): String? {
    return appleMusic?.artistName?.takeIf { it.isNotBlank() }
        ?: deezerJson?.artist?.name?.takeIf { it.isNotBlank() }
        ?: artist?.takeIf { it.isNotBlank() }
        ?: spotify?.artists?.firstOrNull()?.name?.takeIf { it.isNotBlank() }
        ?: napster?.artistName?.takeIf { it.isNotBlank() }
        ?: musicbrainz?.firstOrNull()?.artistCredit?.firstOrNull()?.name?.takeIf { it.isNotBlank() }
}

private fun AuddResponseJson.Result.parseAlbum(): String? {
    return appleMusic?.albumName?.takeIf { it.isNotBlank() }
        ?: deezerJson?.album?.title?.takeIf { it.isNotBlank() }
        ?: album?.takeIf { it.isNotBlank() }
        ?: spotify?.album?.name?.takeIf { it.isNotBlank() }
        ?: napster?.albumName?.takeIf { it.isNotBlank() }
}

private fun AuddResponseJson.Result.parseTrackDuration(): Duration? {
    return appleMusic?.durationInMillis?.toLong()?.milliseconds
        ?: spotify?.durationMillis?.toLong()?.milliseconds
        ?: deezerJson?.durationSeconds?.toLong()?.seconds
        ?: napster?.durationSeconds?.toLong()?.seconds
        ?: musicbrainz?.firstOrNull()?.durationMillis?.toLong()?.milliseconds
}

private fun parseRecognizedAt(input: String): Duration? {
    return try {
        val timeParts = input.split(":").map { it.toLong() }
        when (timeParts.size) {
            1 -> timeParts[0].seconds
            2 -> timeParts[0].minutes + timeParts[1].seconds
            3 -> timeParts[0].hours + timeParts[1].minutes + timeParts[2].seconds
            else -> null
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to parse track duration", e)
        null
    }
}

private fun String.toLocalDate(): LocalDate? {
    return try {
        LocalDate.parse(this, DateTimeFormatter.ISO_DATE)
    } catch (e: DateTimeParseException) {
        Log.e(TAG, "Failed to parse track release date", e)
        null
    }
}

private fun AuddResponseJson.Result.parseReleaseDate(): LocalDate? {
    return appleMusic?.releaseDate?.toLocalDate()
        ?: deezerJson?.releaseDate?.toLocalDate()
        ?: releaseDate?.toLocalDate()
        ?: spotify?.album?.releaseDate?.toLocalDate()
}

private fun isUrlValid(potentialUrl: String) = potentialUrl.toHttpUrlOrNull() != null
private fun String.replaceHttpWithHttps() = replaceFirst("http://", "https://", true)
private fun String.takeUrlIfValid() = replaceHttpWithHttps().takeIf { isUrlValid(it) }

private fun AuddResponseJson.Result.toTrackArtwork(): TrackArtwork? {
    arrayOf(true, false).forEach { requireHiRes ->
        val deezerAlbum = TrackArtwork(
            url = deezerJson?.album?.toArtworkLink(requireHiRes),
            thumbUrl = deezerJson?.album?.toArtworkThumbnailLink()
        )
        if (deezerAlbum.thumbUrl != null && deezerAlbum.url != null) return deezerAlbum

        val appleMusic = TrackArtwork(
            url = appleMusic?.artwork?.toArtworkLink(requireHiRes),
            thumbUrl = appleMusic?.artwork?.toArtworkThumbnailLink()
        )
        if (appleMusic.thumbUrl != null && appleMusic.url != null) return appleMusic

        val spotify = TrackArtwork(
            url = spotify?.album?.toArtworkLink(requireHiRes),
            thumbUrl = spotify?.album?.toArtworkThumbnailLink()
        )
        if (spotify.thumbUrl != null && spotify.url != null) return spotify

        val deezerArtist = TrackArtwork(
            url = deezerJson?.artist?.toArtworkLink(requireHiRes),
            thumbUrl = deezerJson?.artist?.toArtworkThumbnailLink()
        )
        if (deezerArtist.thumbUrl != null && deezerArtist.url != null) return deezerArtist

        if (deezerAlbum.url != null) return deezerAlbum
        if (appleMusic.url != null) return appleMusic
        if (spotify.url != null) return spotify
        if (deezerArtist.url != null) return deezerArtist
    }
    return null
}

private fun DeezerJson.Album.toArtworkLink(requireHiRes: Boolean) =
    (coverXl ?: coverBig.takeIf { !requireHiRes })?.takeUrlIfValid()

private fun DeezerJson.Album.toArtworkThumbnailLink(): String? =
    coverMedium?.takeUrlIfValid()

private fun DeezerJson.Artist.toArtworkLink(requireHiRes: Boolean) =
    (pictureXl ?: pictureBig.takeIf { !requireHiRes })?.takeUrlIfValid()

private fun DeezerJson.Artist.toArtworkThumbnailLink(): String? =
    pictureMedium?.takeUrlIfValid()

private fun AppleMusicJson.Artwork.toArtworkLink(requireHiRes: Boolean): String? {
    if (width == null || height == null || url == null) return null
    if (!isEnoughArtworkSize(width, height)) return null
    if (requireHiRes && isLowResArtwork(width, height)) return null
    val desiredSize = 1500
    val isDesiredAvailable = width >= desiredSize && height >= desiredSize
    val selectedRes = if (isDesiredAvailable) "${desiredSize}x$desiredSize" else "${width}x$height"
    return url.replaceFirst("{w}x{h}", selectedRes, true).takeUrlIfValid()
}

private fun AppleMusicJson.Artwork.toArtworkThumbnailLink(): String? {
    if (width == null || height == null || url == null) return null
    if (!isEnoughArtworkThumbnailSize(width, height)) return null
    val desiredSize = 300
    val isDesiredAvailable = width >= desiredSize && height >= desiredSize
    val selectedRes = if (isDesiredAvailable) "${desiredSize}x$desiredSize" else "${width}x$height"
    return url.replaceFirst("{w}x{h}", selectedRes, true).takeUrlIfValid()
}

private fun isEnoughArtworkSize(width: Int, height: Int) = width >= 500 && height >= 500
private fun isEnoughArtworkThumbnailSize(width: Int, height: Int) = width >= 250 && height >= 250
private fun isLowResArtwork(width: Int, height: Int) = width < 1000 || height < 1000

// assume that the first one in the list is the highest res
private fun SpotifyJson.Album.toArtworkLink(requireHiRes: Boolean): String? {
    return images?.firstNotNullOfOrNull { image ->
        image?.run {
            if (width == null || height == null || url == null) return null
            if (!isEnoughArtworkSize(width, height)) return null
            if (requireHiRes && isLowResArtwork(width, height)) return null
            url.takeUrlIfValid()
        }
    }
}
private fun SpotifyJson.Album.toArtworkThumbnailLink(): String? {
    return images?.firstNotNullOfOrNull { image ->
        image?.run {
            if (width == null || height == null || url == null) return null
            if (!isEnoughArtworkThumbnailSize(width, height)) return null
            if (width >= 400 || height >= 400) return null
            url.takeUrlIfValid()
        }
    }
}

private fun AuddResponseJson.Result.parseSpotifyLink() =
    spotify?.externalUrls?.spotify?.takeUrlIfValid()

private fun AuddResponseJson.Result.parseAppleMusicLink() =
    appleMusic?.url?.takeUrlIfValid()

private fun List<LyricsJson.MediaItem>.parseYoutubeLink() =
    firstOrNull { item -> item.provider == "youtube" }?.url?.takeUrlIfValid()

private fun List<LyricsJson.MediaItem>.parseSoundCloudLink() =
    firstOrNull { item -> item.provider == "soundcloud" }?.url?.takeUrlIfValid()

private fun AuddResponseJson.Result.parseMusicBrainzLink() =
    musicbrainz?.firstOrNull()?.id?.run { "https://musicbrainz.org/recording/$this" }
        ?.takeIf { isUrlValid(it) }

private fun AuddResponseJson.Result.parseDeezerLink() =
    deezerJson?.link?.takeUrlIfValid()

private fun AuddResponseJson.Result.parseNapsterLink() =
    napster?.id?.run { "https://web.napster.com/track/$this" }?.takeIf { isUrlValid(it) }

private fun AuddResponseJson.Result.parseLyrics() = this.lyricsJson?.lyrics?.run {
    Html.fromHtml(
        this.replace("\n", "<br>"),
        Html.FROM_HTML_MODE_COMPACT
    ).toString().trim().takeIf { it.isNotBlank() }?.run(::PlainLyrics)
}

@Suppress("unused")
private fun AuddResponseJson.Result.parseArtworkSeedColor(): Int? {
    return this.appleMusic?.artwork?.backgroundColor?.run {
        try {
            "#$this".toColorInt()
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Failed to parse artwork color", e)
            null
        }
    }
}

private fun parseErrorResult(error: AuddResponseJson.Error): RemoteRecognitionResult {
    return when (error.errorCode) {
        300, 400, 500 -> RemoteRecognitionResult.Error.BadRecording(error.errorMessage)
        900 -> RemoteRecognitionResult.Error.AuthError
        901 -> RemoteRecognitionResult.Error.ApiUsageLimited
        else -> RemoteRecognitionResult.Error.UnhandledError(
            message = "Audd error response\n" +
                    "code=${error.errorCode}\n" +
                    "message=${error.errorMessage}"
        )
    }
}

private fun getUnknownError(): RemoteRecognitionResult {
    val message = "Unknown AudD error response"
    return RemoteRecognitionResult.Error.UnhandledError(message)
}

/*
https://docs.audd.io/#common-errors
We have about 40 different error codes. The common errors:

    #901 — No api_token passed, and the limit was reached (you need to obtain an api_token).
    #900 — Wrong API token (check the api_token parameter).
    #600 — Incorrect audio url.
    #700 — You haven't sent a file for recognition (or we didn't receive it).
    If you use the POST HTTP method, check the Content-Type header: it should be multipart/form-data;
    also check the URL you're sending requests to: it should start with https://
    (http:// requests get redirected,
    and we don't receive any data from you when your code follows the redirect).
    #500 — Incorrect audio file.
    #400 — Too big audio file. 10M or 25 seconds is the maximum.
    We recommend recording no more than 20 seconds (usually, it takes less than one megabyte).
    #300 — Fingerprinting error: there was a problem with audio decoding or with the neural network.
    Possibly, the audio file is too small.
    #100 — An unknown error. Contact us in this case.

*/
