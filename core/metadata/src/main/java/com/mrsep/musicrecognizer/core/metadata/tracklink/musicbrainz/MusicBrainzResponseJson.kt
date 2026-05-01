package com.mrsep.musicrecognizer.core.metadata.tracklink.musicbrainz

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class MusicBrainzRecordingSearchJson(
    @SerialName("created")
    val created: String?,
    @SerialName("count")
    val count: Int?,
    @SerialName("offset")
    val offset: Int?,
    @SerialName("recordings")
    val recordings: List<MusicBrainzRecordingJson?>?,
)

@Serializable
internal data class MusicBrainzRecordingJson(
    @SerialName("id")
    val id: String?,
    @SerialName("score")
    val score: Int?,
    @SerialName("title")
    val title: String?,
    @SerialName("length")
    val length: Long?,
    @SerialName("video")
    val video: Boolean?,
    @SerialName("disambiguation")
    val disambiguation: String?,
    @SerialName("artist-credit")
    val artistCredit: List<ArtistCredit?>?,
    @SerialName("isrcs")
    val isrcs: List<String?>?,
) {

    @Serializable
    internal data class ArtistCredit(
        @SerialName("name")
        val name: String?,
        @SerialName("joinphrase")
        val joinphrase: String?,
        @SerialName("artist")
        val artist: Artist?,
    ) {

        @Serializable
        internal data class Artist(
            @SerialName("id")
            val id: String?,
            @SerialName("name")
            val name: String?,
            @SerialName("sort-name")
            val sortName: String?,
            @SerialName("disambiguation")
            val disambiguation: String?,
        )
    }
}
