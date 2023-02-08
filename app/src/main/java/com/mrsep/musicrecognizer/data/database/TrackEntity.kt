package com.mrsep.musicrecognizer.data.database

import androidx.room.*

@Entity(tableName = "track", indices = [Index(
    value = ["title", "artist", "album"],
    unique = true
)])
data class TrackEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "artist")
    val artist: String,
    @ColumnInfo(name = "album")
    val album: String?,
    @ColumnInfo(name = "release_date")
    val releaseDate: Long?,
    @ColumnInfo(name = "lyrics")
    val lyrics: String?,
    @Embedded(prefix = "link_")
    val links: Links?,
) {

    data class Links(
        @ColumnInfo(name = "artwork")
        val artwork: String?,
        @ColumnInfo(name = "spotify")
        val spotify: String?,
        @ColumnInfo(name = "youtube")
        val youtube: String?,
        @ColumnInfo(name = "apple_music")
        val appleMusic: String?,
        @ColumnInfo(name = "music_brainz")
        val musicBrainz: String?,
        @ColumnInfo(name = "deezer")
        val deezer: String?,
        @ColumnInfo(name = "napster")
        val napster: String?
    )

}