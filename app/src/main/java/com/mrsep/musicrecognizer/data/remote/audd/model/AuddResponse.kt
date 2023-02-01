package com.mrsep.musicrecognizer.data.remote.audd.model

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Json

@JsonClass(generateAdapter = true)
data class AuddResponse(
    @Json(name = "status")
    val status: String,
    @Json(name = "result")
    val result: Result?
) {

    @JsonClass(generateAdapter = true)
    data class Result(
        @Json(name = "artist")
        val artist: String,
        @Json(name = "title")
        val title: String,
        @Json(name = "album")
        val album: String,
        @Json(name = "release_date")
        val releaseDate: String,
        @Json(name = "label")
        val label: String,
        @Json(name = "timecode")
        val timecode: String,
        @Json(name = "song_link")
        val songLink: String,
        @Json(name = "deezer")
        val deezer: Deezer?,
        @Json(name = "lyrics")
        val lyrics: Lyrics?,
//        @Json(name = "musicbrainz")
//        val musicbrainz: List<Musicbrainz>?,
//        @Json(name = "napster")
//        val napster: Napster?,
//        @Json(name = "spotify")
//        val spotify: Spotify?
    )

    override fun toString() =
        result?.run {
            artist + " - " + title + "\n" +
                    album + " " + releaseDate + "\n\n" +
                    lyrics?.lyrics.orEmpty()
        } ?: "$status\nnull result"

}