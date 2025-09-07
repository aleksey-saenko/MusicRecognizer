package com.mrsep.musicrecognizer.core.recognition.audd.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class MusicbrainzJson(
    @SerialName("id")
    val id: String?,
    @SerialName("title")
    val title: String?,
    @SerialName("length")
    val durationMillis: Int?,
    @SerialName("artist-credit")
    val artistCredit: List<ArtistCredit?>?
) {

    @Serializable
    data class ArtistCredit(
        @SerialName("name")
        val name: String?
    )
}
