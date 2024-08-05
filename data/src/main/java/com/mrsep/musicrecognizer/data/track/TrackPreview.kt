package com.mrsep.musicrecognizer.data.track

import androidx.room.ColumnInfo
import java.time.Instant

data class TrackPreview(
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "artist")
    val artist: String,
    @ColumnInfo(name = "album")
    val album: String?,
    @ColumnInfo(name = "recognition_date")
    val recognitionDate: Instant,
    @ColumnInfo(name = "link_artwork_thumb")
    val artworkThumbnail: String?,
    @ColumnInfo(name = "link_artwork")
    val artwork: String?,
    @ColumnInfo(name = "is_viewed")
    val isViewed: Boolean,
)
