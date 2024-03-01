package com.mrsep.musicrecognizer.data.remote.acrcloud.json

import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionResultDo
import com.mrsep.musicrecognizer.data.track.TrackEntity
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.UUID

internal fun AcrCloudResponseJson.toRecognitionResult(): RemoteRecognitionResultDo {
    return when (status.code) {
        0 -> tryToParseSuccessResult(this)
        1001 -> RemoteRecognitionResultDo.NoMatches
        2000, 2004 -> RemoteRecognitionResultDo.Error.BadRecording(getErrorMessage(this))
        3001, 3014 -> RemoteRecognitionResultDo.Error.AuthError
        3003, 3015 -> RemoteRecognitionResultDo.Error.ApiUsageLimited
        else -> RemoteRecognitionResultDo.Error.UnhandledError(getErrorMessage((this)))
    }
}

private fun tryToParseSuccessResult(json: AcrCloudResponseJson): RemoteRecognitionResultDo {
    return json.metadata?.music?.firstOrNull()?.run(::tryToParseMusic)
        ?: json.metadata?.humming?.firstOrNull()?.run(::tryToParseMusic)
        ?: RemoteRecognitionResultDo.NoMatches
}

private fun tryToParseMusic(
    music: AcrCloudResponseJson.Metadata.Music
): RemoteRecognitionResultDo.Success? {
    val title = music.title
    val artist = music.artists?.mapNotNull { it.name }?.joinToString(" & ")
    if (title == null || artist == null) return null

    val track = TrackEntity(
        // TODO: must not be random to define the same track
        id = UUID.randomUUID().toString(),
        title = music.title,
        artist = artist,
        album = music.album?.name,
        releaseDate = music.releaseDate?.run(::parseReleaseDate),
        duration = music.durationMs?.toLong()?.run(Duration::ofMillis),
        recognizedAt = music.playOffsetMs?.toLong()?.run(Duration::ofMillis),
        lyrics = null,
        links = TrackEntity.Links(
            artwork = null,
            amazonMusic = null,
            anghami = null,
            appleMusic = null,
            audiomack = null,
            audius = null,
            boomplay = null,
            deezer = parseDeezerUrl(music),
            musicBrainz = null,
            napster = null,
            pandora = null,
            soundCloud = null,
            spotify = parseSpotifyUrl(music),
            tidal = null,
            yandexMusic = null,
            youtube = parseYoutubeUrl(music),
            youtubeMusic = null
        ),
        properties = TrackEntity.Properties(
            lastRecognitionDate = Instant.now(),
            isFavorite = false,
            themeSeedColor = null
        )
    )
    return RemoteRecognitionResultDo.Success(track)
}

private fun parseDeezerUrl(music: AcrCloudResponseJson.Metadata.Music): String? {
    return music.externalMetadata?.deezer?.track?.id?.run {
        "https://www.deezer.com/track/$this"
    }
}

private fun parseSpotifyUrl(music: AcrCloudResponseJson.Metadata.Music): String? {
    return music.externalMetadata?.spotify?.track?.id?.run {
        "https://open.spotify.com/track/$this"
    }
}

private fun parseYoutubeUrl(music: AcrCloudResponseJson.Metadata.Music): String? {
    return music.externalMetadata?.youtube?.vid?.run {
        "https://www.youtube.com/watch?v=$this"
    }
}

private fun parseReleaseDate(releaseDate: String): LocalDate? {
    if (releaseDate == "None") return null
    return try {
        LocalDate.parse(releaseDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    } catch (e: DateTimeParseException) {
        val year = releaseDate.toIntOrNull()?.takeIf { it in 1700..LocalDate.now().year }
        year?.run { LocalDate.of(year, 1, 1) }
    }
}

private fun getErrorMessage(json: AcrCloudResponseJson): String {
    return "ACRCloud error response: ${json.status.msg}"
}

/* https://docs.acrcloud.com/sdk-reference/error-codes
* Error codes:
* 0 - Recognition succeed
* 1001 - No recognition result
* 2000 - Recording error (device may not have permission)
* 2001 - Init failed or request timeout
* 2002 - Metadata parse error
* 2004 - Unable to generate fingerprint
* 2005 - Timeout
* 3000 - Recognition service error (http error 500)
* 3001 - Missing/Invalid Access Key
* 3003 - Limit exceeded, please upgrade your account
* 3006 - Invalid arguments
* 3014 - Invalid signature
* 3015 - QpS limit exceeded, please upgrade your account
* */