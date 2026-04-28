package com.mrsep.musicrecognizer.core.recognition.enhancer.qobuz

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class QobuzAutosuggestResponseJson(
    @SerialName("tracks")
    val tracks: List<Track?>?,
) {
    @Serializable
    internal data class Track(
        @SerialName("id")
        val id: Long?,
        @SerialName("title")
        val title: String?,
        @SerialName("album")
        val album: String?,
        @SerialName("artist")
        val artist: String?,
        @SerialName("image")
        val image: String?,
        @SerialName("url")
        val url: String?,
    )
}
