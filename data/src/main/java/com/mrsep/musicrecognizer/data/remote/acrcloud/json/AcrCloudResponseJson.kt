package com.mrsep.musicrecognizer.data.remote.acrcloud.json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class AcrCloudResponseJson(
    @Json(name = "cost_time")
    val costTime: Double?,
    @Json(name = "result_type")
    val resultType: Int?,
    @Json(name = "metadata")
    val metadata: Metadata?,
    @Json(name = "status")
    val status: Status
) {
    @JsonClass(generateAdapter = true)
    data class Metadata(
        @Json(name = "music")
        val music: List<Music>?,
        @Json(name = "humming")
        val humming: List<Music>?,
        @Json(name = "timestamp_utc")
        val timestampUtc: String?
    ) {

        @JsonClass(generateAdapter = true)
        data class Music(
            @Json(name = "title")
            val title: String?,
            @Json(name = "album")
            val album: Album?,
            @Json(name = "label")
            val label: String?,
            @Json(name = "external_metadata")
            val externalMetadata: ExternalMetadata?,
            @Json(name = "release_date")
            val releaseDate: String?,
            @Json(name = "artists")
            val artists: List<Artist>?,
            @Json(name = "duration_ms")
            val durationMs: Int?,
            @Json(name = "external_ids")
            val externalIds: ExternalIds?,
            @Json(name = "db_end_time_offset_ms")
            val dbEndTimeOffsetMs: Int?,
            @Json(name = "score")
            val score: Double?,
            @Json(name = "sample_end_time_offset_ms")
            val sampleEndTimeOffsetMs: Int?,
            @Json(name = "play_offset_ms")
            val playOffsetMs: Int?,
            @Json(name = "result_from")
            val resultFrom: Int?,
            @Json(name = "db_begin_time_offset_ms")
            val dbBeginTimeOffsetMs: Int?,
            @Json(name = "sample_begin_time_offset_ms")
            val sampleBeginTimeOffsetMs: Int?,
            @Json(name = "genres")
            val genres: List<Genre>?,
            @Json(name = "acrid")
            val acrid: String?
        ) {

            @JsonClass(generateAdapter = true)
            data class Album(
                @Json(name = "name")
                val name: String?
            )

            @JsonClass(generateAdapter = true)
            data class ExternalMetadata(
                @Json(name = "deezer")
                val deezer: Deezer?,
                @Json(name = "spotify")
                val spotify: Spotify?,
                @Json(name = "youtube")
                val youtube: Youtube?
            ) {
                @JsonClass(generateAdapter = true)
                data class Deezer(
                    @Json(name = "album")
                    val album: Album?,
                    @Json(name = "artists")
                    val artists: List<Artist>?,
                    @Json(name = "track")
                    val track: Track?
                ) {
                    @JsonClass(generateAdapter = true)
                    data class Album(
                        @Json(name = "name")
                        val name: String?,
                        @Json(name = "id")
                        val id: Int?
                    )

                    @JsonClass(generateAdapter = true)
                    data class Artist(
                        @Json(name = "name")
                        val name: String?,
                        @Json(name = "id")
                        val id: Int?
                    )

                    @JsonClass(generateAdapter = true)
                    data class Track(
                        @Json(name = "id")
                        val id: String?,
                        @Json(name = "name")
                        val name: String?
                    )
                }

                @JsonClass(generateAdapter = true)
                data class Spotify(
                    @Json(name = "album")
                    val album: Album?,
                    @Json(name = "artists")
                    val artists: List<Artist>?,
                    @Json(name = "track")
                    val track: Track?
                ) {
                    @JsonClass(generateAdapter = true)
                    data class Album(
                        @Json(name = "name")
                        val name: String?,
                        @Json(name = "id")
                        val id: String?
                    )

                    @JsonClass(generateAdapter = true)
                    data class Artist(
                        @Json(name = "name")
                        val name: String?,
                        @Json(name = "id")
                        val id: String?
                    )

                    @JsonClass(generateAdapter = true)
                    data class Track(
                        @Json(name = "id")
                        val id: String?,
                        @Json(name = "name")
                        val name: String?
                    )
                }

                @JsonClass(generateAdapter = true)
                data class Youtube(
                    @Json(name = "vid")
                    val vid: String?
                )
            }

            @JsonClass(generateAdapter = true)
            data class Artist(
                @Json(name = "name")
                val name: String?
            )

            @JsonClass(generateAdapter = true)
            data class ExternalIds(
                @Json(name = "upc")
                val upc: String?,
                @Json(name = "isrc")
                val isrc: String?
            )

            @JsonClass(generateAdapter = true)
            data class Genre(
                @Json(name = "name")
                val name: String?,
                @Json(name = "id")
                val id: Int?
            )
        }
    }

    @JsonClass(generateAdapter = true)
    data class Status(
        @Json(name = "version")
        val version: String,
        @Json(name = "msg")
        val msg: String,
        @Json(name = "code")
        val code: Int
    )
}
