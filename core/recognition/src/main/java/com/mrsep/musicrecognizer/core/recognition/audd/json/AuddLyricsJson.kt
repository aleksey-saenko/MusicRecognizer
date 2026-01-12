package com.mrsep.musicrecognizer.core.recognition.audd.json

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

@Serializable
internal data class AuddLyricsJson(
    @SerialName("lyrics")
    val lyrics: String?,
    @SerialName("media")
    @Serializable(with = MediaListAsStringSerializer::class)
    val media: List<MediaItem>?,
) {

    @Serializable
    data class MediaItem(
        @SerialName("provider")
        val provider: String?,
        @SerialName("url")
        val url: String?
    )
}

private object MediaListAsStringSerializer : KSerializer<List<AuddLyricsJson.MediaItem>?> {
    private val listSerializer = ListSerializer(AuddLyricsJson.MediaItem.serializer())
    override val descriptor: SerialDescriptor = listSerializer.descriptor

    override fun deserialize(decoder: Decoder): List<AuddLyricsJson.MediaItem>? {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("MediaListAsStringSerializer can be used only with Json")
        val element = jsonDecoder.decodeJsonElement()

        return when {
            element is JsonArray -> jsonDecoder.json.decodeFromJsonElement(listSerializer, element)
            element is JsonPrimitive && element.isString -> {
                val content = element.content
                if (content.isBlank()) null
                else jsonDecoder.json.decodeFromString(listSerializer, content)
            }
            element is JsonNull -> null
            else -> throw SerializationException("Unexpected JSON for media field: $element")
        }
    }

    override fun serialize(encoder: Encoder, value: List<AuddLyricsJson.MediaItem>?) {
        error("Not implemented")
    }
}
