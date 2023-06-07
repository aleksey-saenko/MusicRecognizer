package com.mrsep.musicrecognizer.data.remote.audd.json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MusicbrainzJson(
    @Json(name = "id")
    val id: String,
    @Json(name = "score")
    val score: Int,
    @Json(name = "title")
    val title: String,
    @Json(name = "length")
    val length: Int,
    @Json(name = "disambiguation")
    val disambiguation: String,
    @Json(name = "video")
    val video: Any?,
    @Json(name = "artist-credit")
    val artistCredit: List<ArtistCredit?>?,
    @Json(name = "releases")
    val releases: List<Release>,
    @Json(name = "isrcs")
    val isrcs: List<String>,
    @Json(name = "tags")
    val tags: List<Tag>?
) {
    @JsonClass(generateAdapter = true)
    data class ArtistCredit(
        @Json(name = "name")
        val name: String,
        @Json(name = "artist")
        val artist: Artist
    ) {
        @JsonClass(generateAdapter = true)
        data class Artist(
            @Json(name = "id")
            val id: String,
            @Json(name = "name")
            val name: String,
            @Json(name = "sort-name")
            val sortName: String
        )
    }

    @JsonClass(generateAdapter = true)
    data class Release(
        @Json(name = "id")
        val id: String,
        @Json(name = "count")
        val count: Int,
        @Json(name = "title")
        val title: String,
        @Json(name = "status")
        val status: String,
        @Json(name = "date")
        val date: String,
        @Json(name = "country")
        val country: String,
        @Json(name = "release-events")
        val releaseEvents: List<ReleaseEvent>?,
        @Json(name = "track-count")
        val trackCount: Int,
        @Json(name = "media")
        val media: List<Media>,
        @Json(name = "artist-credit")
        val artistCredit: List<ArtistCredit?>?,
        @Json(name = "release-group")
        val releaseGroup: ReleaseGroup,
        @Json(name = "disambiguation")
        val disambiguation: String?
    ) {
        @JsonClass(generateAdapter = true)
        data class ReleaseEvent(
            @Json(name = "date")
            val date: String,
            @Json(name = "area")
            val area: Area
        ) {
            @JsonClass(generateAdapter = true)
            data class Area(
                @Json(name = "id")
                val id: String,
                @Json(name = "name")
                val name: String,
                @Json(name = "sort-name")
                val sortName: String,
                @Json(name = "iso-3166-1-codes")
                val iso31661Codes: List<String>
            )
        }

        @JsonClass(generateAdapter = true)
        data class Media(
            @Json(name = "position")
            val position: Int,
            @Json(name = "format")
            val format: String,
            @Json(name = "track")
            val track: List<Track>,
            @Json(name = "track-count")
            val trackCount: Int,
            @Json(name = "track-offset")
            val trackOffset: Int
        ) {
            @JsonClass(generateAdapter = true)
            data class Track(
                @Json(name = "id")
                val id: String,
                @Json(name = "number")
                val number: String,
                @Json(name = "title")
                val title: String,
                @Json(name = "length")
                val length: Int
            )
        }

        @JsonClass(generateAdapter = true)
        data class ArtistCredit(
            @Json(name = "name")
            val name: String,
            @Json(name = "artist")
            val artist: Artist
        ) {
            @JsonClass(generateAdapter = true)
            data class Artist(
                @Json(name = "id")
                val id: String,
                @Json(name = "name")
                val name: String,
                @Json(name = "sort-name")
                val sortName: String,
                @Json(name = "disambiguation")
                val disambiguation: String
            )
        }

        @JsonClass(generateAdapter = true)
        data class ReleaseGroup(
            @Json(name = "id")
            val id: String,
            @Json(name = "type-id")
            val typeId: String,
            @Json(name = "title")
            val title: String,
            @Json(name = "primary-type")
            val primaryType: String,
            @Json(name = "secondary-types")
            val secondaryTypes: List<String>?
        )
    }

    @JsonClass(generateAdapter = true)
    data class Tag(
        @Json(name = "count")
        val count: Int,
        @Json(name = "name")
        val name: String
    )
}