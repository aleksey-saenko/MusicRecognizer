package com.mrsep.musicrecognizer.data.remote.audd.json

import android.graphics.Color
import android.text.Html
import android.util.Patterns
import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionResultDo
import com.mrsep.musicrecognizer.data.track.TrackEntity
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

internal fun AuddResponseJson.toRecognitionResult(): RemoteRecognitionResultDo {
    return when (status) {
        "success" -> if (result == null) {
            RemoteRecognitionResultDo.NoMatches
        } else {
            parseSuccessResult(result)
        }

        "error" -> if (error == null) {
            getUnknownError()
        } else {
            parseErrorResult(error)
        }

        else -> getUnknownError()
    }
}

private fun parseSuccessResult(result: AuddResponseJson.Result): RemoteRecognitionResultDo {
    val trackTitle = result.parseTrackTitle()
    val trackArtist = result.parseTrackArtist()
    if (trackTitle.isNullOrBlank() || trackArtist.isNullOrBlank()) {
        return RemoteRecognitionResultDo.NoMatches
    }
    val mediaItems = result.lyricsJson?.parseMediaItems()
    val trackId = result.musicbrainz?.firstOrNull()?.id ?: UUID.randomUUID().toString()

    return RemoteRecognitionResultDo.Success(
        data = TrackEntity(
            id = trackId,
            title = trackTitle,
            artist = trackArtist,
            album = result.parseAlbum(),
            releaseDate = result.parseReleaseDate(),
            duration = result.parseTrackDuration(),
            recognizedAt = result.timecode?.run(::parseRecognizedAt),
            lyrics = result.parseLyrics(),
            links = TrackEntity.Links(
                artwork = result.toArtworkLink(),
                amazonMusic = null,
                anghami = null,
                appleMusic = result.parseAppleMusicLink(),
                audiomack = null,
                audius = null,
                boomplay = null,
                deezer = result.parseDeezerLink(),
                musicBrainz = result.parseMusicBrainzLink(),
                napster = result.parseNapsterLink(),
                pandora = null,
                soundCloud = mediaItems?.parseSoundCloudLink(),
                spotify = result.parseSpotifyLink(),
                tidal = null,
                yandexMusic = null,
                youtube = mediaItems?.parseYoutubeLink(),
                youtubeMusic = null
            ),
            properties = TrackEntity.Properties(
                lastRecognitionDate = Instant.now(),
                isFavorite = false,
                //TODO: need to test matching the color by palette
                themeSeedColor = null //json.result.parseArtworkSeedColor()
            )
        )
    )
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
    return appleMusic?.durationInMillis?.toLong()?.run(Duration::ofMillis)
        ?: spotify?.durationMillis?.toLong()?.run(Duration::ofMillis)
        ?: deezerJson?.durationSeconds?.toLong()?.run(Duration::ofSeconds)
        ?: napster?.durationSeconds?.toLong()?.run(Duration::ofSeconds)
        ?: musicbrainz?.firstOrNull()?.durationMillis?.toLong()?.run(Duration::ofMillis)
}

private fun parseRecognizedAt(input: String): Duration? {
    return runCatching {
        val timeParts = input.split(":").map { it.toLong() }
        when (timeParts.size) {
            1 -> Duration.ofSeconds(timeParts[0])
            2 -> Duration.ofMinutes(timeParts[0]).plusSeconds(timeParts[1])
            3 -> Duration.ofHours(timeParts[0]).plusMinutes(timeParts[1]).plusSeconds(timeParts[2])
            else -> null
        }
    }.getOrNull()
}

private fun String.toLocalDate() =
    runCatching { LocalDate.parse(this, DateTimeFormatter.ISO_DATE) }.getOrNull()

private fun AuddResponseJson.Result.parseReleaseDate(): LocalDate? {
    return appleMusic?.releaseDate?.toLocalDate()
        ?: deezerJson?.releaseDate?.toLocalDate()
        ?: releaseDate?.toLocalDate()
        ?: spotify?.album?.releaseDate?.toLocalDate()
}

private fun isUrlValid(potentialUrl: String) = Patterns.WEB_URL.matcher(potentialUrl).matches()
private fun String.replaceHttpWithHttps() =
    replaceFirst("http://", "https://", true)

private fun String.takeUrlIfValid() = replaceHttpWithHttps().takeIf { isUrlValid(it) }

private fun AuddResponseJson.Result.toArtworkLink(): String? {
    return deezerJson?.album?.run { coverXl ?: coverBig }?.takeUrlIfValid()
        ?: appleMusic?.artwork?.toArtworkLink(true)?.takeUrlIfValid()
        ?: spotify?.album?.toArtworkLink(true)?.takeUrlIfValid()
        ?: deezerJson?.artist?.run { pictureXl ?: pictureBig }?.takeUrlIfValid()

        ?: appleMusic?.artwork?.toArtworkLink(false)?.takeUrlIfValid()
        ?: spotify?.album?.toArtworkLink(false)?.takeUrlIfValid()
        ?: deezerJson?.album?.run { coverMedium ?: coverSmall }?.takeUrlIfValid()
        ?: deezerJson?.artist?.run { pictureMedium ?: pictureSmall }?.takeUrlIfValid()
}

// assume that the first one in the list is the highest res
private fun SpotifyJson.Album.toArtworkLink(requireHiRes: Boolean): String? {
    return images?.firstOrNull()?.run {
        if (width == null || height == null || url == null) return null
        if (requireHiRes && isLowResArtwork(width, height)) return null
        url
    }
}

private fun AppleMusicJson.Artwork.toArtworkLink(requireHiRes: Boolean): String? {
    if (width == null || height == null || url == null) return null
    if (requireHiRes && isLowResArtwork(width, height)) return null
    val isDefaultResAvailable = width >= 1000 && height >= 1000
    val selectedRes = if (isDefaultResAvailable) "1000x1000" else "${width}x${height}"
    return url.replaceFirst("{w}x{h}", selectedRes, true)
}

private fun isLowResArtwork(width: Int, height: Int) = width < 700 || height < 700

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
    ).toString().trim().takeIf { it.isNotBlank() }
}

private fun AuddResponseJson.Result.parseArtworkSeedColor() =
    this.appleMusic?.artwork?.backgroundColor?.run {
        runCatching { Color.parseColor("#$this") }.getOrNull()
    }


private fun parseErrorResult(error: AuddResponseJson.Error): RemoteRecognitionResultDo {
    return when (error.errorCode) {
        300, 400, 500 -> RemoteRecognitionResultDo.Error.BadRecording(error.errorMessage)
        900 -> RemoteRecognitionResultDo.Error.AuthError
        901 -> RemoteRecognitionResultDo.Error.ApiUsageLimited
        else -> RemoteRecognitionResultDo.Error.UnhandledError(
            message = "Audd error response\n" +
                    "code=${error.errorCode}\n" +
                    "message=${error.errorMessage}"
        )
    }
}

private fun getUnknownError(): RemoteRecognitionResultDo {
    val message = "Unknown AudD error response"
    return RemoteRecognitionResultDo.Error.UnhandledError(message)
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