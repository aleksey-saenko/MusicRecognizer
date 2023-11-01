package com.mrsep.musicrecognizer.data.remote.audd.json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class AppleMusicJson(
    @Json(name = "artwork")
    val artwork: Artwork?,
    @Json(name = "artistName")
    val artistName: String?,
    @Json(name = "url")
    val url: String?,
    @Json(name = "releaseDate")
    val releaseDate: String?,
    @Json(name = "name")
    val name: String?,
    @Json(name = "albumName")
    val albumName: String?,
) {

    @JsonClass(generateAdapter = true)
    data class Artwork(
        @Json(name = "url")
        val url: String?,
        @Json(name = "bgColor")
        val backgroundColor: String?
    )

}