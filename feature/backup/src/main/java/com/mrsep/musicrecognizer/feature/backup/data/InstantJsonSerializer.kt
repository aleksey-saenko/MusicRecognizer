package com.mrsep.musicrecognizer.feature.backup.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant

internal object InstantJsonSerializer : KSerializer<Instant> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "InstantBackupJsonSerializer",
        PrimitiveKind.LONG
    )

    override fun serialize(encoder: Encoder, value: Instant) = encoder.encodeLong(value.epochSecond)

    override fun deserialize(decoder: Decoder): Instant = Instant.ofEpochSecond(decoder.decodeLong())
}
