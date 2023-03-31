package com.mrsep.musicrecognizer.data.track

import androidx.room.*
import java.time.Instant
import java.time.LocalDate

/*
 * MusicBrainz Recording Identifier uses as id (mbId)
 * https://musicbrainz.org/doc/MusicBrainz_Identifier
 */
@Entity(tableName = "track")
data class TrackEntity(
    @PrimaryKey @ColumnInfo(name = "mb_id")
    val mbId: String,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "artist")
    val artist: String,
    @ColumnInfo(name = "album")
    val album: String?,
    @ColumnInfo(name = "release_date")
    val releaseDate: LocalDate?,
    @ColumnInfo(name = "lyrics")
    val lyrics: String?,
    @Embedded(prefix = "link_")
    val links: Links,
    @Embedded
    val metadata: Metadata
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
        @ColumnInfo(name = "musicbrainz")
        val musicBrainz: String?,
        @ColumnInfo(name = "deezer")
        val deezer: String?,
        @ColumnInfo(name = "napster")
        val napster: String?
    )

    data class Metadata(
        @ColumnInfo(name = "last_recognition_date")
        val lastRecognitionDate: Instant,
        @ColumnInfo(name = "is_favorite")
        val isFavorite: Boolean
    )

}