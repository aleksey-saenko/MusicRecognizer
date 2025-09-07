package com.mrsep.musicrecognizer.core.recognition.audd.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class AppleMusicJson(
    @SerialName("artwork")
    val artwork: Artwork?,
    @SerialName("artistName")
    val artistName: String?,
    @SerialName("url")
    val url: String?,
    @SerialName("durationInMillis")
    val durationInMillis: Int?,
    @SerialName("releaseDate")
    val releaseDate: String?,
    @SerialName("name")
    val name: String?,
    @SerialName("albumName")
    val albumName: String?,
) {

    @Serializable
    data class Artwork(
        @SerialName("width")
        val width: Int?,
        @SerialName("height")
        val height: Int?,
        @SerialName("url")
        val url: String?,
        @SerialName("bgColor")
        val backgroundColor: String?
    )
}
