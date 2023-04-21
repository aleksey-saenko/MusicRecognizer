package com.mrsep.musicrecognizer.data.remote.audd.model

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Json

sealed class AuddResponseJson {

    @JsonClass(generateAdapter = true)
    data class Success(
        @Json(name = "status")
        val status: String,
        @Json(name = "result")
        val result: Result?
    ) : AuddResponseJson() {

        @JsonClass(generateAdapter = true)
        data class Result(
            @Json(name = "artist")
            val artist: String,
            @Json(name = "title")
            val title: String,
            @Json(name = "album")
            val album: String?,
            @Json(name = "release_date")
            val releaseDate: String?,
            @Json(name = "label")
            val label: String?,
            @Json(name = "timecode")
            val timecode: String?,
            @Json(name = "song_link")
            val songLink: String?,
            @Json(name = "deezer")
            val deezerJson: DeezerJson?,
            @Json(name = "lyrics")
            val lyricsJson: LyricsJson?,
            @Json(name = "musicbrainz")
            val musicbrainz: List<MusicbrainzJson>?,
            @Json(name = "napster")
            val napster: NapsterJson?,
            @Json(name = "spotify")
            val spotify: SpotifyJson?,
            @Json(name = "apple_music")
            val appleMusic: AppleMusicJson?,
        )

    }


    @JsonClass(generateAdapter = true)
    data class Error(
        @Json(name = "status")
        val status: String,
        @Json(name = "error")
        val body: Body
    ) : AuddResponseJson() {

        @JsonClass(generateAdapter = true)
        data class Body(
            @Json(name = "error_code")
            val errorCode: Int,
            @Json(name = "error_message")
            val errorMessage: String
        )
    }

}



