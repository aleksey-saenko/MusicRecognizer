package com.mrsep.musicrecognizer.data.remote.audd.model.adapter

import android.text.Html
import com.mrsep.musicrecognizer.data.remote.audd.model.AuddResponseJson
import com.mrsep.musicrecognizer.domain.model.RemoteRecognizeResult
import com.mrsep.musicrecognizer.domain.model.Track
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class AuddJsonAdapter {

    @FromJson
    fun fromJson(json: AuddResponseJson): RemoteRecognizeResult<Track> {
        return when (json) {
            is AuddResponseJson.Error -> fromErrorJson(json)
            is AuddResponseJson.Success -> fromSuccessJson(json)
        }
    }

    @ToJson
    fun toJson(
        @Suppress("UNUSED_PARAMETER") recognizeResponse: RemoteRecognizeResult<Track>
    ): AuddResponseJson =
        throw IllegalStateException("Not implemented")

    private fun fromSuccessJson(json: AuddResponseJson.Success): RemoteRecognizeResult<Track> {
        return when (json.result) {
            null -> RemoteRecognizeResult.NoMatches
            else -> RemoteRecognizeResult.Success(
                data = Track(
                    mbId = json.result.musicbrainz?.firstOrNull()?.id ?: UUID.randomUUID().toString(),
                    artist = json.result.artist,
                    title = json.result.title,
                    album = json.result.album,
                    releaseDate = json.result.releaseDate?.toLocalDate(),
                    lyrics = json.result.lyricsJson?.lyrics?.decodeHtml(),
                    links = Track.Links(
                        artwork = json.result.deezerJson?.album?.coverBig, //FIXME write logic for priority artwork parsing
                        spotify = null,
                        appleMusic = null,
                        youtube = null,
                        musicBrainz = null,
                        deezer = null,
                        napster = null
                    ),
                    metadata = Track.Metadata(
                        lastRecognitionDate = System.currentTimeMillis(),
                        isFavorite = false
                    )
                )
            )
        }
    }

    private fun fromErrorJson(json: AuddResponseJson.Error): RemoteRecognizeResult<Track> {
        return when (json.body.errorCode) {
            901 -> RemoteRecognizeResult.Error.LimitReached
            900 -> RemoteRecognizeResult.Error.WrongToken
            else -> RemoteRecognizeResult.Error.UnhandledError(
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

private val dateParser = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
private fun String.toLocalDate() = runCatching { LocalDate.parse(this, dateParser) }.getOrNull()
//private fun String.toEpochSeconds() =
//    runCatching { LocalDate.parse(this, dateParser) }.getOrNull()?.toEpochDay()

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