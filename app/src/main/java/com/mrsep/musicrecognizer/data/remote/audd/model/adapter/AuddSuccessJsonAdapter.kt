package com.mrsep.musicrecognizer.data.remote.audd.model.adapter

import com.mrsep.musicrecognizer.data.remote.audd.model.AuddResponseJson
import com.mrsep.musicrecognizer.domain.model.RecognizeResult
import com.mrsep.musicrecognizer.domain.model.Track
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class AuddSuccessJsonAdapter {

    @FromJson
    fun fromJson(json: AuddResponseJson.Success): RecognizeResult<Track> {
        return when (json.result) {
            null -> RecognizeResult.NoMatches
            else -> RecognizeResult.Success(
                data = Track(
                    artist = json.result.artist,
                    title = json.result.title,
                    album = json.result.album,
                    releaseDate = json.result.releaseDate?.toLocalDate(),
                    lyrics = json.result.lyricsJson?.lyrics,
                    links = Track.Links(
                        artwork = json.result.deezerJson?.album?.coverBig, //FIXME write logic for priority artwork parsing
                        spotify = null,
                        appleMusic = null,
                        youtube = null,
                        musicBrainz = null,
                        deezer = null,
                        napster = null
                    )
                )
            )
        }
    }

    @ToJson
    fun toJson(
        @Suppress("UNUSED_PARAMETER") recognizeResponse: RecognizeResult<Track>
    ): AuddResponseJson.Success =
        throw IllegalStateException("Not implemented")

}

private val dateParser = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
private fun String.toLocalDate(): LocalDate? =
    kotlin.runCatching { LocalDate.parse(this, dateParser) }.getOrNull()