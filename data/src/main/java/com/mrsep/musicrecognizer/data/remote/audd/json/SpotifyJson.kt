package com.mrsep.musicrecognizer.data.remote.audd.json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class SpotifyJson(
    @Json(name = "album")
    val album: Album?,
    @Json(name = "artists")
    val artists: List<Artist?>?,
    @Json(name = "duration_ms")
    val durationMillis: Int?,
    @Json(name = "external_urls")
    val externalUrls: ExternalUrls?,
    @Json(name = "name")
    val name: String?
) {
    @JsonClass(generateAdapter = true)
    data class Album(
        @Json(name = "images")
        val images: List<Image?>?,
        @Json(name = "name")
        val name: String?,
        @Json(name = "release_date")
        val releaseDate: String?
    ) {

        @JsonClass(generateAdapter = true)
        data class Image(
            @Json(name = "height")
            val height: Int?,
            @Json(name = "width")
            val width: Int?,
            @Json(name = "url")
            val url: String?
        )
    }

    @JsonClass(generateAdapter = true)
    data class Artist(
        @Json(name = "name")
        val name: String?
    )

    @JsonClass(generateAdapter = true)
    data class ExternalUrls(
        @Json(name = "spotify")
        val spotify: String?
    )
}
