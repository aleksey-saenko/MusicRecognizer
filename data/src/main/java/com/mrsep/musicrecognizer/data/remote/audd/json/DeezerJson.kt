package com.mrsep.musicrecognizer.data.remote.audd.json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class DeezerJson(
    @Json(name = "title")
    val title: String?,
    @Json(name = "link")
    val link: String?,
    @Json(name = "release_date")
    val releaseDate: String?,
    @Json(name = "artist")
    val artist: Artist?,
    @Json(name = "album")
    val album: Album?,
) {

    @JsonClass(generateAdapter = true)
    data class Artist(
        @Json(name = "name")
        val name: String?
    )

    @JsonClass(generateAdapter = true)
    data class Album(
        @Json(name = "title")
        val title: String?,
        @Json(name = "cover")
        val cover: String?,
        @Json(name = "cover_small")
        val coverSmall: String?,
        @Json(name = "cover_medium")
        val coverMedium: String?,
        @Json(name = "cover_big")
        val coverBig: String?,
        @Json(name = "cover_xl")
        val coverXl: String?
    )

}