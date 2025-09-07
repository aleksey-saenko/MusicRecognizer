package com.mrsep.musicrecognizer.core.recognition.enhancer.odesli

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal object OdesliApiProviderSerializer : KSerializer<OdesliApiProvider> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "OdesliApiProviderSerializer",
        PrimitiveKind.STRING
    )

    override fun serialize(encoder: Encoder, value: OdesliApiProvider) {
        error("Not implemented (unused)")
    }

    override fun deserialize(decoder: Decoder) = when (decoder.decodeString()) {
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
}
