package com.mrsep.musicrecognizer.data.track

import androidx.room.*
import java.time.Instant
import java.time.LocalDate

@Entity(tableName = "track")
data class TrackEntity(
    @PrimaryKey @ColumnInfo(name = "id")
    val id: String,
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
    val properties: Properties
) {

    data class Links(
        @ColumnInfo(name = "artwork")
        val artwork: String?,
        @ColumnInfo(name = "amazon_music")
        val amazonMusic: String?,
        @ColumnInfo(name = "anghami")
        val anghami: String?,
        @ColumnInfo(name = "apple_music")
        val appleMusic: String?,
        @ColumnInfo(name = "audiomack")
        val audiomack: String?,
        @ColumnInfo(name = "audius")
        val audius: String?,
        @ColumnInfo(name = "boomplay")
        val boomplay: String?,
        @ColumnInfo(name = "deezer")
        val deezer: String?,
        @ColumnInfo(name = "musicbrainz")
        val musicBrainz: String?,
        @ColumnInfo(name = "napster")
        val napster: String?,
        @ColumnInfo(name = "pandora")
        val pandora: String?,
        @ColumnInfo(name = "soundcloud")
        val soundCloud: String?,
        @ColumnInfo(name = "spotify")
        val spotify: String?,
        @ColumnInfo(name = "tidal")
        val tidal: String?,
        @ColumnInfo(name = "yandex_music")
        val yandexMusic: String?,
        @ColumnInfo(name = "youtube")
        val youtube: String?,
        @ColumnInfo(name = "youtube_music")
        val youtubeMusic: String?,
    )

    data class Properties(
        @ColumnInfo(name = "last_recognition_date")
        val lastRecognitionDate: Instant,
        @ColumnInfo(name = "is_favorite")
        val isFavorite: Boolean,
        @ColumnInfo(name = "theme_seed_color")
        val themeSeedColor: Int?,
    )

}

