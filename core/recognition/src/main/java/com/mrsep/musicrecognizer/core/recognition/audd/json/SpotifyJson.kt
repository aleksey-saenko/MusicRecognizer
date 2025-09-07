package com.mrsep.musicrecognizer.core.recognition.audd.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class SpotifyJson(
    @SerialName("album")
    val album: Album?,
    @SerialName("artists")
    val artists: List<Artist?>?,
    @SerialName("duration_ms")
    val durationMillis: Int?,
    @SerialName("external_urls")
    val externalUrls: ExternalUrls?,
    @SerialName("name")
    val name: String?
) {
    @Serializable
    data class Album(
        @SerialName("images")
        val images: List<Image?>?,
        @SerialName("name")
        val name: String?,
        @SerialName("release_date")
        val releaseDate: String?
    ) {

        @Serializable
        data class Image(
            @SerialName("height")
            val height: Int?,
            @SerialName("width")
            val width: Int?,
            @SerialName("url")
            val url: String?
        )
    }

    @Serializable
    data class Artist(
        @SerialName("name")
        val name: String?
    )

    @Serializable
    data class ExternalUrls(
        @SerialName("spotify")
        val spotify: String?
    )
}
