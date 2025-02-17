package com.mrsep.musicrecognizer.core.recognition.audd.json

import android.util.Log
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

@JsonClass(generateAdapter = true)
internal data class LyricsJson(
    @Json(name = "lyrics")
    val lyrics: String?,
    @Json(name = "media")
    val media: String?
) {

    @JsonClass(generateAdapter = true)
    data class MediaItem(
        @Json(name = "provider")
        val provider: String?,
        @Json(name = "url")
        val url: String?
    )

    fun parseMediaItems(): List<MediaItem> {
        if (media.isNullOrBlank()) return emptyList()
        val adapterType = Types.newParameterizedType(
            List::class.java,
            MediaItem::class.java
        )
        val adapter = Moshi.Builder().build().adapter<List<MediaItem>>(adapterType)
        val mediaItems = try {
            adapter.fromJson(media)
        } catch (e: Exception) {
            Log.e(this::class.simpleName, "Error during json parsing", e)
            emptyList()
        }
        return mediaItems ?: emptyList()
    }
}
