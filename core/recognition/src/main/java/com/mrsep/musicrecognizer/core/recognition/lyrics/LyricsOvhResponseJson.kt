package com.mrsep.musicrecognizer.core.recognition.lyrics

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class LyricsOvhResponseJson(
    @SerialName("lyrics")
    val lyrics: String?,
    @SerialName("error")
    val error: String?
)
