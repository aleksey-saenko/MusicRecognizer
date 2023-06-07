package com.mrsep.musicrecognizer.data.remote.audd.json.adapter

import android.text.Html
import android.util.Patterns
import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionResultDo
import com.mrsep.musicrecognizer.data.remote.audd.json.AuddResponseJson
import com.mrsep.musicrecognizer.data.remote.audd.json.LyricsJson
import com.mrsep.musicrecognizer.data.remote.audd.json.parseMediaItems
import com.mrsep.musicrecognizer.data.track.TrackEntity
import com.squareup.moshi.*
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


class AuddJsonAdapter {

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
        throw IllegalStateException("Not implemented")


    private fun fromSuccessJson(json: AuddResponseJson.Success): RemoteRecognitionResultDo {
        return when (json.result) {
            null -> RemoteRecognitionResultDo.NoMatches
            else -> {
                val mediaItems = json.result.lyricsJson?.media?.let { parseMediaItems(it) }
                RemoteRecognitionResultDo.Success(
                    data = TrackEntity(
                        mbId = json.result.musicbrainz?.firstOrNull()?.id ?: UUID.randomUUID()
                            .toString(),
                        artist = json.result.artist,
                        title = json.result.title,
                        album = json.result.album,
                        releaseDate = json.result.releaseDate?.toLocalDate(),
                        lyrics = json.result.lyricsJson?.lyrics?.decodeHtml()?.trim(),
                        links = TrackEntity.Links(
                            artwork = json.result.deezerJson?.album?.coverBig?.let { url ->
                                validUrlOrNull(url)
                            }, //FIXME write logic for priority artwork parsing
                            spotify = json.result.spotify?.externalUrls?.spotify?.let { url ->
                                validUrlOrNull(url)
                            },
                            appleMusic = json.result.appleMusic?.url?.let { url ->
                                validUrlOrNull(url)
                            },
                            youtube = mediaItems?.parseYoutubeLink()?.let { url ->
                                validUrlOrNull(url)
                            },
                            soundCloud = mediaItems?.parseSoundCloudLink()?.let { url ->
                                validUrlOrNull(url)
                            },
                            musicBrainz = json.result.musicbrainz?.firstOrNull()?.id
                                ?.let { makeMusicBrainzRecordingUrl(it) }?.let { url ->
                                    validUrlOrNull(url)
                                },
                            deezer = json.result.deezerJson?.link?.let { url ->
                                validUrlOrNull(url)
                            },
                            napster = json.result.napster?.id?.let { makeNapsterUrl(it) }
                                ?.let { url ->
                                    validUrlOrNull(url)
                                }
                        ),
                        metadata = TrackEntity.Metadata(
                            lastRecognitionDate = Instant.now(),
                            isFavorite = false
                        )
                    )
                )
            }
        }
    }

    private fun fromErrorJson(json: AuddResponseJson.Error): RemoteRecognitionResultDo {
        return when (json.body.errorCode) {
            300, 400, 500 -> RemoteRecognitionResultDo.Error.BadRecording(json.body.errorMessage)
            901 -> RemoteRecognitionResultDo.Error.WrongToken(isLimitReached = true)
            900 -> RemoteRecognitionResultDo.Error.WrongToken(isLimitReached = false)
            else -> RemoteRecognitionResultDo.Error.UnhandledError(
                message = "Audd response error\n" +
                        "code=${json.body.errorCode}\n" +
                        "message=${json.body.errorMessage}"
            )
        }
    }

}

private fun String.decodeHtml(): String {
    val preparedString = this.replace("\n", "<br>")
    return Html.fromHtml(preparedString, Html.FROM_HTML_MODE_COMPACT).toString()
}

private fun String.toLocalDate() =
    runCatching { LocalDate.parse(this, DateTimeFormatter.ISO_DATE) }.getOrNull()

private fun makeMusicBrainzRecordingUrl(mbId: String) = "https://musicbrainz.org/recording/$mbId"

private fun makeNapsterUrl(id: String) = "https://web.napster.com/track/$id"

private fun List<LyricsJson.MediaItem>.parseYoutubeLink() =
    firstOrNull { item -> item.provider == "youtube" }?.url

private fun List<LyricsJson.MediaItem>.parseSoundCloudLink() =
    firstOrNull { item -> item.provider == "soundcloud" }?.url

fun isUrlValid(potentialUrl: String) = Patterns.WEB_URL.matcher(potentialUrl).matches()
fun validUrlOrNull(potentialUrl: String) = if (isUrlValid(potentialUrl)) potentialUrl else null


/*
https://docs.audd.io/#common-errors
We have about 40 different error codes. The API returns the errors with an explanation of what happened. The common errors:

    #901 — No api_token passed, and the limit was reached (you need to obtain an api_token).
    #900 — Wrong API token (check the api_token parameter).
    #600 — Incorrect audio url.
    #700 — You haven't sent a file for recognition (or we didn't receive it). If you use the POST HTTP method, check the Content-Type header: it should be multipart/form-data; also check the URL you're sending requests to: it should start with https:// (http:// requests get redirected and we don't receive any data from you when your code follows the redirect).
    #500 — Incorrect audio file.
    #400 — Too big audio file. 10M or 25 seconds is the maximum. We recommend recording no more than 20 seconds (usually, it takes less than one megabyte). If you need to recognize larger audio files, use the enterprise endpoint instead, it supports even days-long files.
    #300 — Fingerprinting error: there was a problem with audio decoding or with the neural network. Possibly, the audio file is too small.
    #100 — An unknown error. Contact us in this case.
 */