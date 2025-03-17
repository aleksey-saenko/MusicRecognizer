package com.mrsep.musicrecognizer.core.recognition.enhancer.odesli

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class OdesliResponseJson(
    @SerialName("entityUniqueId")
    val entityUniqueId: String?,
    @SerialName("userCountry")
    val userCountry: String?,
    @SerialName("pageUrl")
    val pageUrl: String?,
    @SerialName("entitiesByUniqueId")
    val entitiesByUniqueId: Map<String, Entity>?,
    @SerialName("linksByPlatform")
    val linksByPlatform: LinksByPlatform?,
) {

    @Serializable
    data class Entity(
        @SerialName("id")
        val id: String?,
        @SerialName("type")
        val type: String?,
        @SerialName("title")
        val title: String?,
        @SerialName("artistName")
        val artistName: String?,
        @SerialName("thumbnailUrl")
        val thumbnailUrl: String?,
        @SerialName("thumbnailWidth")
        val thumbnailWidth: Int?,
        @SerialName("thumbnailHeight")
        val thumbnailHeight: Int?,
        @SerialName("apiProvider")
        val apiProvider: OdesliApiProvider?,
    )

    @Serializable
    data class LinksByPlatform(
        @SerialName("amazonMusic")
        val amazonMusic: AmazonMusic?,
        @SerialName("amazonStore")
        val amazonStore: AmazonStore?,
        @SerialName("audiomack")
        val audiomack: Audiomack?,
        @SerialName("audius")
        val audius: Audius?,
        @SerialName("anghami")
        val anghami: Anghami?,
        @SerialName("boomplay")
        val boomplay: Boomplay?,
        @SerialName("appleMusic")
        val appleMusic: AppleMusic?,
        @SerialName("spotify")
        val spotify: Spotify?,
        @SerialName("youtube")
        val youtube: Youtube?,
        @SerialName("youtubeMusic")
        val youtubeMusic: YoutubeMusic?,
        @SerialName("google")
        val google: Google?,
        @SerialName("pandora")
        val pandora: Pandora?,
        @SerialName("deezer")
        val deezer: Deezer?,

        @SerialName("soundcloud")
        val soundcloud: Soundcloud?,
        @SerialName("tidal")
        val tidal: Tidal?,
        @SerialName("napster")
        val napster: Napster?,
        @SerialName("yandex")
        val yandex: Yandex?,
        @SerialName("itunes")
        val itunes: Itunes?,
        @SerialName("googleStore")
        val googleStore: GoogleStore?,

        ) {

        @Serializable
        data class AmazonMusic(
            @SerialName("url")
            val url: String?
        )

        @Serializable
        data class AmazonStore(
            @SerialName("url")
            val url: String?
        )

        @Serializable
        data class Audiomack(
            @SerialName("url")
            val url: String?
        )

        @Serializable
        data class Audius(
            @SerialName("url")
            val url: String?
        )

        @Serializable
        data class Anghami(
            @SerialName("url")
            val url: String?
        )

        @Serializable
        data class Boomplay(
            @SerialName("url")
            val url: String?
        )

        @Serializable
        data class AppleMusic(
            @SerialName("url")
            val url: String?
        )

        @Serializable
        data class Spotify(
            @SerialName("url")
            val url: String?
        )

        @Serializable
        data class Youtube(
            @SerialName("url")
            val url: String?
        )

        @Serializable
        data class YoutubeMusic(
            @SerialName("url")
            val url: String?
        )

        @Serializable
        data class Google(
            @SerialName("url")
            val url: String?
        )

        @Serializable
        data class Pandora(
            @SerialName("url")
            val url: String?
        )

        @Serializable
        data class Deezer(
            @SerialName("url")
            val url: String?
        )

        @Serializable
        data class Soundcloud(
            @SerialName("url")
            val url: String?
        )

        @Serializable
        data class Tidal(
            @SerialName("url")
            val url: String?
        )

        @Serializable
        data class Napster(
            @SerialName("url")
            val url: String?
        )

        @Serializable
        data class Yandex(
            @SerialName("url")
            val url: String?
        )

        @Serializable
        data class Itunes(
            @SerialName("url")
            val url: String?
        )

        @Serializable
        data class GoogleStore(
            @SerialName("url")
            val url: String?
        )
    }
}

@Serializable
internal data class OdesliErrorResponseJson(
    @SerialName("statusCode")
    val code: Int?,
    @SerialName("code")
    val message: String?,
)

// Ordered in artwork parsing priority
@Serializable(with = OdesliApiProviderSerializer::class)
internal enum class OdesliApiProvider {
    Spotify,
    Itunes,
    Deezer,
    Amazon,
    Napster,
    Tidal,
    Pandora,
    Soundcloud,
    Youtube,
    Yandex,
    Spinrilla,
    Audius,
    Audiomack,
    Anghami,
    Boomplay,
    Google,
    Unknown,
}
