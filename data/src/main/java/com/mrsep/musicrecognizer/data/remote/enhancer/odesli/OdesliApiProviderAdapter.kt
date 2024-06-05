package com.mrsep.musicrecognizer.data.remote.enhancer.odesli

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

internal class OdesliApiProviderAdapter {

    @FromJson
    fun fromJson(json: String) = when (json) {
        "spotify" -> OdesliApiProvider.Spotify
        "itunes" -> OdesliApiProvider.Itunes
        "deezer" -> OdesliApiProvider.Deezer
        "amazon" -> OdesliApiProvider.Amazon
        "napster" -> OdesliApiProvider.Napster
        "tidal" -> OdesliApiProvider.Tidal
        "pandora" -> OdesliApiProvider.Pandora
        "soundcloud" -> OdesliApiProvider.Soundcloud
        "youtube" -> OdesliApiProvider.Youtube
        "yandex" -> OdesliApiProvider.Yandex
        "spinrilla" -> OdesliApiProvider.Spinrilla
        "audius" -> OdesliApiProvider.Audius
        "audiomack" -> OdesliApiProvider.Audiomack
        "anghami" -> OdesliApiProvider.Anghami
        "boomplay" -> OdesliApiProvider.Boomplay
        "google" -> OdesliApiProvider.Google
        else -> OdesliApiProvider.Unknown
    }

    @ToJson
    fun toJson(
        @Suppress("UNUSED_PARAMETER") provider: OdesliApiProvider
    ): String =
        throw IllegalStateException("Not implemented (unused)")
}
