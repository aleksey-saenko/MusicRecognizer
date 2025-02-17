package com.mrsep.musicrecognizer.core.recognition.enhancer.odesli

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class OdesliResponseJson(
    @Json(name = "entityUniqueId")
    val entityUniqueId: String?,
    @Json(name = "userCountry")
    val userCountry: String?,
    @Json(name = "pageUrl")
    val pageUrl: String?,
    @Json(name = "entitiesByUniqueId")
    val entitiesByUniqueId: Map<String, Entity>?,
    @Json(name = "linksByPlatform")
    val linksByPlatform: LinksByPlatform?,
) {

    @JsonClass(generateAdapter = true)
    data class Entity(
        @Json(name = "id")
        val id: String?,
        @Json(name = "type")
        val type: String?,
        @Json(name = "title")
        val title: String?,
        @Json(name = "artistName")
        val artistName: String?,
        @Json(name = "thumbnailUrl")
        val thumbnailUrl: String?,
        @Json(name = "thumbnailWidth")
        val thumbnailWidth: Int?,
        @Json(name = "thumbnailHeight")
        val thumbnailHeight: Int?,
        @Json(name = "apiProvider")
        val apiProvider: OdesliApiProvider?,
    )

    @JsonClass(generateAdapter = true)
    data class LinksByPlatform(
        @Json(name = "amazonMusic")
        val amazonMusic: AmazonMusic?,
        @Json(name = "amazonStore")
        val amazonStore: AmazonStore?,
        @Json(name = "audiomack")
        val audiomack: Audiomack?,
        @Json(name = "audius")
        val audius: Audius?,
        @Json(name = "anghami")
        val anghami: Anghami?,
        @Json(name = "boomplay")
        val boomplay: Boomplay?,
        @Json(name = "appleMusic")
        val appleMusic: AppleMusic?,
        @Json(name = "spotify")
        val spotify: Spotify?,
        @Json(name = "youtube")
        val youtube: Youtube?,
        @Json(name = "youtubeMusic")
        val youtubeMusic: YoutubeMusic?,
        @Json(name = "google")
        val google: Google?,
        @Json(name = "pandora")
        val pandora: Pandora?,
        @Json(name = "deezer")
        val deezer: Deezer?,

        @Json(name = "soundcloud")
        val soundcloud: Soundcloud?,
        @Json(name = "tidal")
        val tidal: Tidal?,
        @Json(name = "napster")
        val napster: Napster?,
        @Json(name = "yandex")
        val yandex: Yandex?,
        @Json(name = "itunes")
        val itunes: Itunes?,
        @Json(name = "googleStore")
        val googleStore: GoogleStore?,

        ) {

        @JsonClass(generateAdapter = true)
        data class AmazonMusic(
            @Json(name = "url")
            val url: String?
        )

        @JsonClass(generateAdapter = true)
        data class AmazonStore(
            @Json(name = "url")
            val url: String?
        )

        @JsonClass(generateAdapter = true)
        data class Audiomack(
            @Json(name = "url")
            val url: String?
        )

        @JsonClass(generateAdapter = true)
        data class Audius(
            @Json(name = "url")
            val url: String?
        )

        @JsonClass(generateAdapter = true)
        data class Anghami(
            @Json(name = "url")
            val url: String?
        )

        @JsonClass(generateAdapter = true)
        data class Boomplay(
            @Json(name = "url")
            val url: String?
        )

        @JsonClass(generateAdapter = true)
        data class AppleMusic(
            @Json(name = "url")
            val url: String?
        )

        @JsonClass(generateAdapter = true)
        data class Spotify(
            @Json(name = "url")
            val url: String?
        )

        @JsonClass(generateAdapter = true)
        data class Youtube(
            @Json(name = "url")
            val url: String?
        )

        @JsonClass(generateAdapter = true)
        data class YoutubeMusic(
            @Json(name = "url")
            val url: String?
        )

        @JsonClass(generateAdapter = true)
        data class Google(
            @Json(name = "url")
            val url: String?
        )

        @JsonClass(generateAdapter = true)
        data class Pandora(
            @Json(name = "url")
            val url: String?
        )

        @JsonClass(generateAdapter = true)
        data class Deezer(
            @Json(name = "url")
            val url: String?
        )

        @JsonClass(generateAdapter = true)
        data class Soundcloud(
            @Json(name = "url")
            val url: String?
        )

        @JsonClass(generateAdapter = true)
        data class Tidal(
            @Json(name = "url")
            val url: String?
        )

        @JsonClass(generateAdapter = true)
        data class Napster(
            @Json(name = "url")
            val url: String?
        )

        @JsonClass(generateAdapter = true)
        data class Yandex(
            @Json(name = "url")
            val url: String?
        )

        @JsonClass(generateAdapter = true)
        data class Itunes(
            @Json(name = "url")
            val url: String?
        )

        @JsonClass(generateAdapter = true)
        data class GoogleStore(
            @Json(name = "url")
            val url: String?
        )
    }
}

@JsonClass(generateAdapter = true)
internal data class OdesliErrorResponseJson(
    @Json(name = "statusCode")
    val code: Int?,
    @Json(name = "code")
    val message: String?,
)

// Ordered in artwork parsing priority
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
