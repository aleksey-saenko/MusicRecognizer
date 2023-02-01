package com.mrsep.musicrecognizer.data.remote.audd.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AppleMusic(
    @Json(name = "previews")
    val previews: List<Preview>,
    @Json(name = "artwork")
    val artwork: Artwork,
    @Json(name = "artistName")
    val artistName: String,
    @Json(name = "url")
    val url: String,
    @Json(name = "discNumber")
    val discNumber: Int,
    @Json(name = "genreNames")
    val genreNames: List<String>,
    @Json(name = "durationInMillis")
    val durationInMillis: Int,
    @Json(name = "releaseDate")
    val releaseDate: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "isrc")
    val isrc: String,
    @Json(name = "albumName")
    val albumName: String,
    @Json(name = "playParams")
    val playParams: PlayParams,
    @Json(name = "trackNumber")
    val trackNumber: Int,
    @Json(name = "composerName")
    val composerName: String
) {
    @JsonClass(generateAdapter = true)
    data class Preview(
        @Json(name = "url")
        val url: String
    )

    @JsonClass(generateAdapter = true)
    data class Artwork(
        @Json(name = "width")
        val width: Int,
        @Json(name = "height")
        val height: Int,
        @Json(name = "url")
        val url: String,
        @Json(name = "bgColor")
        val bgColor: String,
        @Json(name = "textColor1")
        val textColor1: String,
        @Json(name = "textColor2")
        val textColor2: String,
        @Json(name = "textColor3")
        val textColor3: String,
        @Json(name = "textColor4")
        val textColor4: String
    )

    @JsonClass(generateAdapter = true)
    data class PlayParams(
        @Json(name = "id")
        val id: String,
        @Json(name = "kind")
        val kind: String
    )
}