package com.mrsep.musicrecognizer.core.recognition.lyrics

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class LyristResponseJson(
    @SerialName("lyrics")
    val lyrics: String?,
    @SerialName("title")
    val title: String?,
    @SerialName("artist")
    val artist: String?,
    @SerialName("image")
    val artworkUrl: String?
)
