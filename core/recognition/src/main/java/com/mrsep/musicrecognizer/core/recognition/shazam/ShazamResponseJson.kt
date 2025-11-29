package com.mrsep.musicrecognizer.core.recognition.shazam

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShazamResponseJson(
    @SerialName("matches")
    val matches: List<Match?>?,
    @SerialName("location")
    val location: Location?,
    @SerialName("timestamp")
    val timestamp: Long?,
    @SerialName("timezone")
    val timezone: String?,
    @SerialName("track")
    val track: Track?,
    @SerialName("tagid")
    val tagid: String?
) {
    @Serializable
    data class Match(
        @SerialName("id")
        val id: String?,
        @SerialName("offset")
        val offset: Double?,
        @SerialName("timeskew")
        val timeskew: Double?,
        @SerialName("frequencyskew")
        val frequencyskew: Double?
    )

    @Serializable
    data class Location(
        @SerialName("latitude")
        val latitude: Double?,
        @SerialName("longitude")
        val longitude: Double?,
        @SerialName("altitude")
        val altitude: Double?,
        @SerialName("accuracy")
        val accuracy: Double?
    )

    @Serializable
    data class Track(
        @SerialName("layout")
        val layout: String?,
        @SerialName("type")
        val type: String?,
        @SerialName("key")
        val key: String?,
        @SerialName("title")
        val title: String?,
        @SerialName("subtitle")
        val subtitle: String?,
        @SerialName("images")
        val images: Images?,
        @SerialName("share")
        val share: Share?,
        @SerialName("hub")
        val hub: Hub?,
        @SerialName("sections")
        val sections: List<Section?>?,
        @SerialName("url")
        val url: String?,
        @SerialName("artists")
        val artists: List<Artist?>?,
        @SerialName("isrc")
        val isrc: String?,
        @SerialName("genres")
        val genres: Genres?,
        @SerialName("urlparams")
        val urlparams: Urlparams?,
        @SerialName("relatedtracksurl")
        val relatedtracksurl: String?,
        @SerialName("albumadamid")
        val albumadamid: String?
    ) {
        @Serializable
        data class Images(
            @SerialName("background")
            val background: String?,
            @SerialName("coverart")
            val coverart: String?,
            @SerialName("coverarthq")
            val coverarthq: String?,
            @SerialName("joecolor")
            val joecolor: String?
        )

        @Serializable
        data class Share(
            @SerialName("subject")
            val subject: String?,
            @SerialName("text")
            val text: String?,
            @SerialName("href")
            val href: String?,
            @SerialName("image")
            val image: String?,
            @SerialName("twitter")
            val twitter: String?,
            @SerialName("html")
            val html: String?,
            @SerialName("avatar")
            val avatar: String?,
            @SerialName("snapchat")
            val snapchat: String?
        )

        @Serializable
        data class Hub(
            @SerialName("type")
            val type: String?,
            @SerialName("image")
            val image: String?,
            @SerialName("actions")
            val actions: List<Action?>?,
            @SerialName("options")
            val options: List<Option?>?,
            @SerialName("providers")
            val providers: List<Provider?>?,
            @SerialName("explicit")
            val explicit: Boolean?,
            @SerialName("displayname")
            val displayname: String?
        ) {
            @Serializable
            data class Action(
                @SerialName("name")
                val name: String?,
                @SerialName("type")
                val type: String?,
                @SerialName("id")
                val id: String?,
                @SerialName("uri")
                val uri: String?
            )

            @Serializable
            data class Option(
                @SerialName("caption")
                val caption: String?,
                @SerialName("actions")
                val actions: List<Action?>?,
                @SerialName("beacondata")
                val beacondata: Beacondata?,
                @SerialName("image")
                val image: String?,
                @SerialName("type")
                val type: String?,
                @SerialName("listcaption")
                val listcaption: String?,
                @SerialName("overflowimage")
                val overflowimage: String?,
                @SerialName("colouroverflowimage")
                val colouroverflowimage: Boolean?,
                @SerialName("providername")
                val providername: String?
            ) {
                @Serializable
                data class Action(
                    @SerialName("name")
                    val name: String?,
                    @SerialName("type")
                    val type: String?,
                    @SerialName("uri")
                    val uri: String?,
                    @SerialName("id")
                    val id: String?
                )

                @Serializable
                data class Beacondata(
                    @SerialName("type")
                    val type: String?,
                    @SerialName("providername")
                    val providername: String?
                )
            }

            @Serializable
            data class Provider(
                @SerialName("caption")
                val caption: String?,
                @SerialName("images")
                val images: Images?,
                @SerialName("actions")
                val actions: List<Action?>?,
                @SerialName("type")
                val type: String?
            ) {
                @Serializable
                data class Images(
                    @SerialName("overflow")
                    val overflow: String?,
                    @SerialName("default")
                    val default: String?
                )

                @Serializable
                data class Action(
                    @SerialName("name")
                    val name: String?,
                    @SerialName("type")
                    val type: String?,
                    @SerialName("uri")
                    val uri: String?
                )
            }
        }

        @Serializable
        data class Section(
            @SerialName("type")
            val type: String?,
            @SerialName("metapages")
            val metapages: List<Metapage?>?,
            @SerialName("tabname")
            val tabname: String?,
            @SerialName("metadata")
            val metadata: List<Metadata?>?,
            @SerialName("url")
            val url: String?,
            @SerialName("text")
            val text: List<String>?,
        ) {
            @Serializable
            data class Metapage(
                @SerialName("image")
                val image: String?,
                @SerialName("caption")
                val caption: String?
            )

            @Serializable
            data class Metadata(
                @SerialName("title")
                val title: String?,
                @SerialName("text")
                val text: String?
            )
        }

        @Serializable
        data class Artist(
            @SerialName("id")
            val id: String?,
            @SerialName("adamid")
            val adamid: String?
        )

        @Serializable
        data class Genres(
            @SerialName("primary")
            val primary: String?
        )

        @Serializable
        data class Urlparams(
            @SerialName("{tracktitle}")
            val tracktitle: String?,
            @SerialName("{trackartist}")
            val trackartist: String?
        )
    }
}
