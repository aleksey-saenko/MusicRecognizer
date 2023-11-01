package com.mrsep.musicrecognizer.data.remote.audd.json.adapter

import android.graphics.Color
import android.text.Html
import android.util.Patterns
import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionResultDo
import com.mrsep.musicrecognizer.data.remote.audd.json.AuddResponseJson
import com.mrsep.musicrecognizer.data.remote.audd.json.LyricsJson
import com.mrsep.musicrecognizer.data.track.TrackEntity
import com.squareup.moshi.*
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

internal class AuddJsonAdapter {

    @FromJson
    fun fromJson(json: AuddResponseJson): RemoteRecognitionResultDo {
        return when (json) {
            is AuddResponseJson.Error -> fromErrorJson(json)
            is AuddResponseJson.Success -> fromSuccessJson(json)
        }
    }

    @ToJson
    fun toJson(
        @Suppress("UNUSED_PARAMETER") recognizeResponse: RemoteRecognitionResultDo
    ): AuddResponseJson =
        throw IllegalStateException("Not implemented (unused)")


    private fun fromSuccessJson(json: AuddResponseJson.Success): RemoteRecognitionResultDo {
        if (json.result == null) return RemoteRecognitionResultDo.NoMatches

        val trackTitle = json.result.parseTrackTitle()
        val trackArtist = json.result.parseTrackArtist()
        if (trackTitle.isNullOrBlank() || trackArtist.isNullOrBlank()) {
            return RemoteRecognitionResultDo.NoMatches
        }
        val mediaItems = json.result.lyricsJson?.parseMediaItems()
        val mbId = json.result.musicbrainz?.firstOrNull()?.id ?: UUID.randomUUID().toString()

        return RemoteRecognitionResultDo.Success(
            data = TrackEntity(
                mbId = mbId,
                title = trackTitle,
                artist = trackArtist,
                album = json.result.parseAlbum(),
                releaseDate = json.result.parseReleaseDate(),
                lyrics = json.result.parseLyrics(),
                links = TrackEntity.Links(
                    artwork = json.result.parseArtworkLink(),
                    spotify = json.result.parseSpotifyLink(),
                    appleMusic = json.result.parseAppleMusicLink(),
                    youtube = mediaItems?.parseYoutubeLink(),
                    soundCloud = mediaItems?.parseSoundCloudLink(),
                    musicBrainz = json.result.parseMusicBrainzLink(),
                    deezer = json.result.parseDeezerLink(),
                    napster = json.result.parseNapsterLink()
                ),
                metadata = TrackEntity.Metadata(
                    lastRecognitionDate = Instant.now(),
                    isFavorite = false,
                    //TODO: need to test matching the color by palette
                    themeSeedColor = null //json.result.parseArtworkSeedColor()
                )
            )
        )
    }

    private fun fromErrorJson(json: AuddResponseJson.Error): RemoteRecognitionResultDo {
        return when (json.body.errorCode) {
            300, 400, 500 -> RemoteRecognitionResultDo.Error.BadRecording(json.body.errorMessage)
            901 -> RemoteRecognitionResultDo.Error.WrongToken(isLimitReached = true)
            900 -> RemoteRecognitionResultDo.Error.WrongToken(isLimitReached = false)
            else -> RemoteRecognitionResultDo.Error.UnhandledError(
                message = "Audd error response\n" +
                        "code=${json.body.errorCode}\n" +
                        "message=${json.body.errorMessage}"
            )
        }
    }

}

private fun AuddResponseJson.Success.Result.parseTrackTitle(): String? {
    return appleMusic?.name?.takeIf { it.isNotBlank() }
        ?: deezerJson?.title?.takeIf { it.isNotBlank() }
        ?: title?.takeIf { it.isNotBlank() }
        ?: spotify?.name?.takeIf { it.isNotBlank() }
        ?: napster?.name?.takeIf { it.isNotBlank() }
        ?: musicbrainz?.firstOrNull()?.title?.takeIf { it.isNotBlank() }
}

private fun AuddResponseJson.Success.Result.parseTrackArtist(): String? {
    return appleMusic?.artistName?.takeIf { it.isNotBlank() }
        ?: deezerJson?.artist?.name?.takeIf { it.isNotBlank() }
        ?: artist?.takeIf { it.isNotBlank() }
        ?: spotify?.artists?.firstOrNull()?.name?.takeIf { it.isNotBlank() }
        ?: napster?.artistName?.takeIf { it.isNotBlank() }
        ?: musicbrainz?.firstOrNull()?.artistCredit?.firstOrNull()?.name?.takeIf { it.isNotBlank() }
}

private fun AuddResponseJson.Success.Result.parseAlbum(): String? {
    return appleMusic?.albumName?.takeIf { it.isNotBlank() }
        ?: deezerJson?.album?.title?.takeIf { it.isNotBlank() }
        ?: album?.takeIf { it.isNotBlank() }
        ?: spotify?.album?.name?.takeIf { it.isNotBlank() }
        ?: napster?.albumName?.takeIf { it.isNotBlank() }
}

private fun String.toLocalDate() =
    runCatching { LocalDate.parse(this, DateTimeFormatter.ISO_DATE) }.getOrNull()

private fun AuddResponseJson.Success.Result.parseReleaseDate(): LocalDate? {
    return appleMusic?.releaseDate?.toLocalDate()
        ?: deezerJson?.releaseDate?.toLocalDate()
        ?: releaseDate?.toLocalDate()
        ?: spotify?.album?.releaseDate?.toLocalDate()
}

private fun isUrlValid(potentialUrl: String) = Patterns.WEB_URL.matcher(potentialUrl).matches()
private fun String.replaceHttpWithHttps() =
    replaceFirst("http://", "https://", true)

private fun AuddResponseJson.Success.Result.parseArtworkLink(): String? {
    return appleMusic?.artwork?.url?.replaceHttpWithHttps()?.takeIf { isUrlValid(it) }
        ?: deezerJson?.album?.run { coverXl ?: coverBig }
            ?.replaceHttpWithHttps()?.takeIf { isUrlValid(it) }
        ?: spotify?.album?.images?.firstOrNull()?.url
            ?.replaceHttpWithHttps()?.takeIf { isUrlValid(it) }
        ?: deezerJson?.album?.run { coverMedium ?: coverSmall }
            ?.replaceHttpWithHttps()?.takeIf { isUrlValid(it) }
}

private fun AuddResponseJson.Success.Result.parseSpotifyLink() =
    spotify?.externalUrls?.spotify?.replaceHttpWithHttps()?.takeIf { isUrlValid(it) }

private fun AuddResponseJson.Success.Result.parseAppleMusicLink() =
    appleMusic?.url?.replaceHttpWithHttps()?.takeIf { isUrlValid(it) }

private fun List<LyricsJson.MediaItem>.parseYoutubeLink() =
    firstOrNull { item -> item.provider == "youtube" }?.url
        ?.replaceHttpWithHttps()?.takeIf { isUrlValid(it) }

private fun List<LyricsJson.MediaItem>.parseSoundCloudLink() =
    firstOrNull { item -> item.provider == "soundcloud" }?.url
        ?.replaceHttpWithHttps()?.takeIf { isUrlValid(it) }

private fun AuddResponseJson.Success.Result.parseMusicBrainzLink() =
    musicbrainz?.firstOrNull()?.id?.run { "https://musicbrainz.org/recording/$this" }
        ?.takeIf { isUrlValid(it) }

private fun AuddResponseJson.Success.Result.parseDeezerLink() =
    deezerJson?.link?.replaceHttpWithHttps()?.takeIf { isUrlValid(it) }

private fun AuddResponseJson.Success.Result.parseNapsterLink() =
    napster?.id?.run { "https://web.napster.com/track/$this" }?.takeIf { isUrlValid(it) }

private fun AuddResponseJson.Success.Result.parseLyrics() = this.lyricsJson?.lyrics?.run {
    Html.fromHtml(
        this.replace("\n", "<br>"),
        Html.FROM_HTML_MODE_COMPACT
    ).toString().trim().takeIf { it.isNotBlank() }
}

private fun AuddResponseJson.Success.Result.parseArtworkSeedColor() =
    this.appleMusic?.artwork?.backgroundColor?.run {
        runCatching { Color.parseColor("#$this") }.getOrNull()
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