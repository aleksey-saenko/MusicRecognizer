package com.mrsep.musicrecognizer.core.recognition.acrcloud.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class AcrCloudResponseJson(
    @SerialName("cost_time")
    val costTime: Double?,
    @SerialName("result_type")
    val resultType: Int?,
    @SerialName("metadata")
    val metadata: Metadata?,
    @SerialName("status")
    val status: Status
) {
    @Serializable
    data class Metadata(
        @SerialName("music")
        val music: List<Music>?,
        @SerialName("humming")
        val humming: List<Music>?,
        @SerialName("timestamp_utc")
        val timestampUtc: String?
    ) {

        @Serializable
        data class Music(
            @SerialName("title")
            val title: String?,
            @SerialName("album")
            val album: Album?,
            @SerialName("label")
            val label: String?,
            @SerialName("external_metadata")
            val externalMetadata: ExternalMetadata?,
            @SerialName("release_date")
            val releaseDate: String?,
            @SerialName("artists")
            val artists: List<Artist>?,
            @SerialName("duration_ms")
            val durationMs: Int?,
            @SerialName("external_ids")
            val externalIds: ExternalIds?,
            @SerialName("db_end_time_offset_ms")
            val dbEndTimeOffsetMs: Int?,
            @SerialName("score")
            val score: Double?,
            @SerialName("sample_end_time_offset_ms")
            val sampleEndTimeOffsetMs: Int?,
            @SerialName("play_offset_ms")
            val playOffsetMs: Int?,
            @SerialName("db_begin_time_offset_ms")
            val dbBeginTimeOffsetMs: Int?,
            @SerialName("sample_begin_time_offset_ms")
            val sampleBeginTimeOffsetMs: Int?,
            @SerialName("genres")
            val genres: List<Genre>?,
            @SerialName("acrid")
            val acrid: String?
        ) {

            @Serializable
            data class Album(
                @SerialName("name")
                val name: String?
            )

            @Serializable
            data class ExternalMetadata(
                @SerialName("deezer")
                val deezer: Deezer?,
                @SerialName("spotify")
                val spotify: Spotify?,
                @SerialName("youtube")
                val youtube: Youtube?
            ) {
                @Serializable
                data class Deezer(
                    @SerialName("album")
                    val album: Album?,
                    @SerialName("artists")
                    val artists: List<Artist>?,
                    @SerialName("track")
                    val track: Track?
                ) {
                    @Serializable
                    data class Album(
                        @SerialName("name")
                        val name: String?,
                        @SerialName("id")
                        val id: Int?
                    )

                    @Serializable
                    data class Artist(
                        @SerialName("name")
                        val name: String?,
                        @SerialName("id")
                        val id: Int?
                    )

                    @Serializable
                    data class Track(
                        @SerialName("id")
                        val id: String?,
                        @SerialName("name")
                        val name: String?
                    )
                }

                @Serializable
                data class Spotify(
                    @SerialName("album")
                    val album: Album?,
                    @SerialName("artists")
                    val artists: List<Artist>?,
                    @SerialName("track")
                    val track: Track?
                ) {
                    @Serializable
                    data class Album(
                        @SerialName("name")
                        val name: String?,
                        @SerialName("id")
                        val id: String?
                    )

                    @Serializable
                    data class Artist(
                        @SerialName("name")
                        val name: String?,
                        @SerialName("id")
                        val id: String?
                    )

                    @Serializable
                    data class Track(
                        @SerialName("id")
                        val id: String?,
                        @SerialName("name")
                        val name: String?
                    )
                }

                @Serializable
                data class Youtube(
                    @SerialName("vid")
                    val vid: String?
                )
            }

            @Serializable
            data class Artist(
                @SerialName("name")
                val name: String?
            )

            @Serializable
            data class ExternalIds(
                @SerialName("upc")
                val upc: String?,
                @SerialName("isrc")
                val isrc: String?
            )

            @Serializable
            data class Genre(
                @SerialName("name")
                val name: String?,
                @SerialName("id")
                val id: Int?
            )
        }
    }

    @Serializable
    data class Status(
        @SerialName("version")
        val version: String,
        @SerialName("msg")
        val msg: String,
        @SerialName("code")
        val code: Int
    )
}
