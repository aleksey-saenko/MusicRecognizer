package com.mrsep.musicrecognizer.data.remote.lyrics

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class LyricsOvhResponseJson(
    val lyrics: String?,
    val error: String?
)