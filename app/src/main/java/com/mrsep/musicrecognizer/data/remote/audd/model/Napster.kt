package com.mrsep.musicrecognizer.data.remote.audd.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Napster(
    @Json(name = "type")
    val type: String,
    @Json(name = "id")
    val id: String,
    @Json(name = "index")
    val index: Int,
    @Json(name = "disc")
    val disc: Int,
    @Json(name = "href")
    val href: String,
    @Json(name = "playbackSeconds")
    val playbackSeconds: Int,
    @Json(name = "isExplicit")
    val isExplicit: Boolean,
    @Json(name = "isStreamable")
    val isStreamable: Boolean,
    @Json(name = "isAvailableInHiRes")
    val isAvailableInHiRes: Boolean,
    @Json(name = "name")
    val name: String,
    @Json(name = "isrc")
    val isrc: String,
    @Json(name = "shortcut")
    val shortcut: String,
    @Json(name = "blurbs")
    val blurbs: List<Any>,
    @Json(name = "artistId")
    val artistId: String,
    @Json(name = "artistName")
    val artistName: String,
    @Json(name = "albumName")
    val albumName: String,
    @Json(name = "formats")
    val formats: List<Format>,
    @Json(name = "losslessFormats")
    val losslessFormats: List<LosslessFormat>,
    @Json(name = "albumId")
    val albumId: String,
    @Json(name = "contributors")
    val contributors: Contributors,
    @Json(name = "links")
    val links: Links,
    @Json(name = "previewURL")
    val previewURL: String
) {
    @JsonClass(generateAdapter = true)
    data class Format(
        @Json(name = "type")
        val type: String,
        @Json(name = "bitrate")
        val bitrate: Int,
        @Json(name = "name")
        val name: String,
        @Json(name = "sampleBits")
        val sampleBits: Int,
        @Json(name = "sampleRate")
        val sampleRate: Int
    )

    @JsonClass(generateAdapter = true)
    data class LosslessFormat(
        @Json(name = "bitrate")
        val bitrate: Int,
        @Json(name = "name")
        val name: String,
        @Json(name = "sampleBits")
        val sampleBits: Int,
        @Json(name = "sampleRate")
        val sampleRate: Int,
        @Json(name = "type")
        val type: String
    )

    @JsonClass(generateAdapter = true)
    data class Contributors(
        @Json(name = "primaryArtist")
        val primaryArtist: String
    )

    @JsonClass(generateAdapter = true)
    data class Links(
        @Json(name = "artists")
        val artists: Artists,
        @Json(name = "albums")
        val albums: Albums,
        @Json(name = "genres")
        val genres: Genres,
        @Json(name = "tags")
        val tags: Tags
    ) {
        @JsonClass(generateAdapter = true)
        data class Artists(
            @Json(name = "ids")
            val ids: List<String>,
            @Json(name = "href")
            val href: String
        )

        @JsonClass(generateAdapter = true)
        data class Albums(
            @Json(name = "ids")
            val ids: List<String>,
            @Json(name = "href")
            val href: String
        )

        @JsonClass(generateAdapter = true)
        data class Genres(
            @Json(name = "ids")
            val ids: List<String>,
            @Json(name = "href")
            val href: String
        )

        @JsonClass(generateAdapter = true)
        data class Tags(
            @Json(name = "ids")
            val ids: List<String>,
            @Json(name = "href")
            val href: String
        )
    }
}