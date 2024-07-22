package com.mrsep.musicrecognizer.data.remote.acrcloud.json

import com.github.f4b6a3.uuid.UuidCreator
import com.mrsep.musicrecognizer.data.remote.RecognitionProviderDo
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
        0 -> parseSuccessResult(this)
        1001 -> RemoteRecognitionResultDo.NoMatches
        2000, 2004 -> RemoteRecognitionResultDo.Error.BadRecording(getErrorMessage(this))
        3001, 3014 -> RemoteRecognitionResultDo.Error.AuthError
        3003, 3015 -> RemoteRecognitionResultDo.Error.ApiUsageLimited
        else -> RemoteRecognitionResultDo.Error.UnhandledError(getErrorMessage((this)))
    }
}

private fun parseSuccessResult(json: AcrCloudResponseJson): RemoteRecognitionResultDo {
    return json.metadata?.music?.firstOrNull()?.run(::parseMusic)
        ?: json.metadata?.humming?.firstOrNull()?.run(::parseMusic)
        ?: RemoteRecognitionResultDo.NoMatches
}

private fun parseMusic(
    music: AcrCloudResponseJson.Metadata.Music
): RemoteRecognitionResultDo.Success? {
    val title = music.title
    val artist = music.artists?.mapNotNull { it.name }?.joinToString(" & ")
    if (title == null || artist == null) return null

    val track = TrackEntity(
        id = createTrackId(music),
        title = music.title,
        artist = artist,
        album = music.album?.name,
        releaseDate = music.releaseDate?.run(::parseReleaseDate),
        duration = music.durationMs?.toLong()?.run(Duration::ofMillis),
        recognizedAt = music.playOffsetMs?.toLong()?.run(Duration::ofMillis),
        recognizedBy = RecognitionProviderDo.AcrCloud,
        recognitionDate = Instant.now(),
        lyrics = null,
        links = TrackEntity.Links(
            artworkThumbnail = null,
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
            isFavorite = false,
            isViewed = false,
            themeSeedColor = null,
        ),
    )
    return RemoteRecognitionResultDo.Success(track)
}

private fun createTrackId(music: AcrCloudResponseJson.Metadata.Music): String {
    return music.acrid?.let {
        // Define some namespace for AcrCloud to distinguish result UUIDs in cases
        // when tracks recognized by different providers have the same remote ID
        val acrCloudIdNamespace = "b2f226c2-e0e8-45d1-884f-856a8c59f174"
        UuidCreator.getNameBasedSha1(acrCloudIdNamespace, music.acrid).toString()
    } ?: UUID.randomUUID().toString()
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
