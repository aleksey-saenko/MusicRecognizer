package com.mrsep.musicrecognizer.core.recognition.lyrics

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class LyricHubResponseJson(
    @SerialName("title")
    val title: String?,
    @SerialName("artist")
    val artist: String?,
    @SerialName("cover")
    val cover: String?,
    @SerialName("lyrics")
    val lyrics: String?,
)
