package com.mrsep.musicrecognizer.data.remote.audd.json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class NapsterJson(
    @Json(name = "id")
    val id: String?,
    @Json(name = "name")
    val name: String?,
    @Json(name = "artistName")
    val artistName: String?,
    @Json(name = "albumName")
    val albumName: String?,
    @Json(name = "playbackSeconds")
    val durationSeconds: Int?
)