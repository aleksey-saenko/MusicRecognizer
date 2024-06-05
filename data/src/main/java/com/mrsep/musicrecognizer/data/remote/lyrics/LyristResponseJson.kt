package com.mrsep.musicrecognizer.data.remote.lyrics

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class LyristResponseJson(
    @Json(name = "lyrics")
    val lyrics: String?,
    @Json(name = "title")
    val title: String?,
    @Json(name = "artist")
    val artist: String?,
    @Json(name = "image")
    val artworkUrl: String?
)
