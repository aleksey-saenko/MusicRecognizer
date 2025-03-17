package com.mrsep.musicrecognizer.core.recognition.lyrics

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class LrcLibResponseJson(
    @SerialName("id")
    val id: Int?,
    @SerialName("trackName")
    val trackName: String?,
    @SerialName("artistName")
    val artistName: String?,
    @SerialName("albumName")
    val albumName: String?,
    @SerialName("duration")
    val duration: Float?,
    @SerialName("instrumental")
    val instrumental: Boolean?,
    @SerialName("plainLyrics")
    val plainLyrics: String?,
    @SerialName("syncedLyrics")
    val syncedLyrics: String?,
)
