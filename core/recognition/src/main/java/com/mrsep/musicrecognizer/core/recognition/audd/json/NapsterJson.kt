package com.mrsep.musicrecognizer.core.recognition.audd.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class NapsterJson(
    @SerialName("id")
    val id: String?,
    @SerialName("name")
    val name: String?,
    @SerialName("artistName")
    val artistName: String?,
    @SerialName("albumName")
    val albumName: String?,
    @SerialName("playbackSeconds")
    val durationSeconds: Int?
)
