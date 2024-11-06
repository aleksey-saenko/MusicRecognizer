package com.mrsep.musicrecognizer.data.remote.lyrics
import com.squareup.moshi.JsonClass

import com.squareup.moshi.Json

@JsonClass(generateAdapter = true)
internal data class LrcLibResponseJson(
    @Json(name = "id")
    val id: Int?,
    @Json(name = "trackName")
    val trackName: String?,
    @Json(name = "artistName")
    val artistName: String?,
    @Json(name = "albumName")
    val albumName: String?,
    @Json(name = "duration")
    val duration: Int?,
    @Json(name = "instrumental")
    val instrumental: Boolean?,
    @Json(name = "plainLyrics")
    val plainLyrics: String?,
    @Json(name = "syncedLyrics")
    val syncedLyrics: String?,
)
