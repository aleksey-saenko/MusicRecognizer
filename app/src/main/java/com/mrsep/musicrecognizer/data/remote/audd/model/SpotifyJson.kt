package com.mrsep.musicrecognizer.data.remote.audd.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SpotifyJson(
    @Json(name = "album")
    val album: Album,
    @Json(name = "artists")
    val artists: List<Artist>,
    @Json(name = "duration_ms")
    val durationMs: Int,
    @Json(name = "external_urls")
    val externalUrls: ExternalUrls,
    @Json(name = "id")
    val id: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "popularity")
    val popularity: Int,
    @Json(name = "track_number")
    val trackNumber: Int,
    @Json(name = "type")
    val type: String,
    @Json(name = "uri")
    val uri: String
) {
    @JsonClass(generateAdapter = true)
    data class Album(
        @Json(name = "album_type")
        val albumType: String,
        @Json(name = "artists")
        val artists: List<Artist>,
        @Json(name = "available_markets")
        val availableMarkets: Any,
        @Json(name = "external_urls")
        val externalUrls: ExternalUrls,
        @Json(name = "href")
        val href: String,
        @Json(name = "id")
        val id: String,
        @Json(name = "images")
        val images: List<Image>,
        @Json(name = "name")
        val name: String,
        @Json(name = "release_date")
        val releaseDate: String,
        @Json(name = "release_date_precision")
        val releaseDatePrecision: String,
        @Json(name = "total_tracks")
        val totalTracks: Int,
        @Json(name = "type")
        val type: String,
        @Json(name = "uri")
        val uri: String
    ) {
        @JsonClass(generateAdapter = true)
        data class Artist(
            @Json(name = "external_urls")
            val externalUrls: ExternalUrls,
            @Json(name = "href")
            val href: String,
            @Json(name = "id")
            val id: String,
            @Json(name = "name")
            val name: String,
            @Json(name = "type")
            val type: String,
            @Json(name = "uri")
            val uri: String
        ) {
            @JsonClass(generateAdapter = true)
            data class ExternalUrls(
                @Json(name = "spotify")
                val spotify: String
            )
        }

        @JsonClass(generateAdapter = true)
        data class ExternalUrls(
            @Json(name = "spotify")
            val spotify: String
        )

        @JsonClass(generateAdapter = true)
        data class Image(
            @Json(name = "height")
            val height: Int,
            @Json(name = "url")
            val url: String,
            @Json(name = "width")
            val width: Int
        )
    }

    @JsonClass(generateAdapter = true)
    data class Artist(
        @Json(name = "external_urls")
        val externalUrls: ExternalUrls,
        @Json(name = "href")
        val href: String,
        @Json(name = "id")
        val id: String,
        @Json(name = "name")
        val name: String,
        @Json(name = "type")
        val type: String,
        @Json(name = "uri")
        val uri: String
    ) {
        @JsonClass(generateAdapter = true)
        data class ExternalUrls(
            @Json(name = "spotify")
            val spotify: String
        )
    }

    @JsonClass(generateAdapter = true)
    data class ExternalIds(
        @Json(name = "isrc")
        val isrc: String
    )

    @JsonClass(generateAdapter = true)
    data class ExternalUrls(
        @Json(name = "spotify")
        val spotify: String
    )
}