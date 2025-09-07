package com.mrsep.musicrecognizer.core.recognition.audd.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class DeezerJson(
    @SerialName("title")
    val title: String?,
    @SerialName("link")
    val link: String?,
    @SerialName("duration")
    val durationSeconds: Int?,
    @SerialName("release_date")
    val releaseDate: String?,
    @SerialName("artist")
    val artist: Artist?,
    @SerialName("album")
    val album: Album?,
) {

    @Serializable
    data class Artist(
        @SerialName("name")
        val name: String?,
        @SerialName("picture")
        val picture: String?,
        @SerialName("picture_small")
        val pictureSmall: String?,
        @SerialName("picture_medium")
        val pictureMedium: String?,
        @SerialName("picture_big")
        val pictureBig: String?,
        @SerialName("picture_xl")
        val pictureXl: String?,
    )

    @Serializable
    data class Album(
        @SerialName("title")
        val title: String?,
        @SerialName("cover")
        val cover: String?,
        @SerialName("cover_small")
        val coverSmall: String?,
        @SerialName("cover_medium")
        val coverMedium: String?,
        @SerialName("cover_big")
        val coverBig: String?,
        @SerialName("cover_xl")
        val coverXl: String?
    )
}
