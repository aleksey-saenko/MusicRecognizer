package com.mrsep.musicrecognizer.data.remote.audd.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LyricsJson(
    @Json(name = "song_id")
    val songId: String,
    @Json(name = "artist_id")
    val artistId: String,
    @Json(name = "title")
    val title: String,
    @Json(name = "title_with_featured")
    val titleWithFeatured: String,
    @Json(name = "full_title")
    val fullTitle: String,
    @Json(name = "artist")
    val artist: String,
    @Json(name = "lyrics")
    val lyrics: String,
    @Json(name = "media")
    val media: String
)