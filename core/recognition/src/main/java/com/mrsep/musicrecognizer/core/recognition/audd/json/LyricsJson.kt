package com.mrsep.musicrecognizer.core.recognition.audd.json

import android.util.Log
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
internal data class LyricsJson(
    @SerialName("lyrics")
    val lyrics: String?,
    @SerialName("media")
    val mediaJson: String?
) {

    @Serializable
    data class MediaItem(
        @SerialName("provider")
        val provider: String?,
        @SerialName("url")
        val url: String?
    )

    fun parseMediaItems(json: Json): List<MediaItem>? {
        if (mediaJson == null) return null
        return try {
            json.decodeFromString<List<MediaItem>>(mediaJson)
        } catch (e: Exception) {
            Log.e(this::class.simpleName, "Failed to parse json media items", e)
            null
        }
    }
}
