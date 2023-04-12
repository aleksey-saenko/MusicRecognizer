package com.mrsep.musicrecognizer.data.remote.audd.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

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
) {

    @JsonClass(generateAdapter = true)
    data class MediaItem(
        @Json(name = "provider")
        val provider: String?,
        @Json(name = "type")
        val type: String?,
        @Json(name = "url")
        val url: String?,
        @Json(name = "native_uri")
        val nativeUri: String?
    )

}


fun parseMediaItems(json: String): List<LyricsJson.MediaItem> {
    if (json.isBlank()) return emptyList()
    val listMyData = Types.newParameterizedType(
        List::class.java,
        LyricsJson.MediaItem::class.java
    )
    val adapter = Moshi.Builder().build().adapter<List<LyricsJson.MediaItem>>(listMyData)
    val mediaItems = try {
        adapter.fromJson(json)
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
    return mediaItems ?: emptyList()
}