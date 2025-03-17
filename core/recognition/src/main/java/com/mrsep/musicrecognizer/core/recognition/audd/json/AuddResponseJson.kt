package com.mrsep.musicrecognizer.core.recognition.audd.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class AuddResponseJson(
    @SerialName("status")
    val status: String,
    @SerialName("result")
    val result: Result?,
    @SerialName("error")
    val error: Error?
) {

    @Serializable
    data class Result(
        @SerialName("artist")
        val artist: String?,
        @SerialName("title")
        val title: String?,
        @SerialName("album")
        val album: String?,
        @SerialName("release_date")
        val releaseDate: String?,
        @SerialName("timecode")
        val timecode: String?,
        @SerialName("song_link")
        val auddSongLink: String?,
        @SerialName("deezer")
        val deezerJson: DeezerJson?,
        @SerialName("lyrics")
        val lyricsJson: LyricsJson?,
        @SerialName("musicbrainz")
        val musicbrainz: List<MusicbrainzJson>?,
        @SerialName("napster")
        val napster: NapsterJson?,
        @SerialName("spotify")
        val spotify: SpotifyJson?,
        @SerialName("apple_music")
        val appleMusic: AppleMusicJson?,
    )

    @Serializable
    data class Error(
        @SerialName("error_code")
        val errorCode: Int,
        @SerialName("error_message")
        val errorMessage: String
    )
}
