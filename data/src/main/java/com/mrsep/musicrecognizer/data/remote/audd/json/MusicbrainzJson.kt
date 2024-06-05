package com.mrsep.musicrecognizer.data.remote.audd.json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class MusicbrainzJson(
    @Json(name = "id")
    val id: String?,
    @Json(name = "title")
    val title: String?,
    @Json(name = "length")
    val durationMillis: Int?,
    @Json(name = "artist-credit")
    val artistCredit: List<ArtistCredit?>?
) {

    @JsonClass(generateAdapter = true)
    data class ArtistCredit(
        @Json(name = "name")
        val name: String?
    )
}
