package com.mrsep.musicrecognizer.core.recognition.shazam

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShazamRequestJson(
    @SerialName("geolocation")
    val geolocation: Geolocation,
    @SerialName("signature")
    val signature: Signature,
    @SerialName("timestamp")
    val timestamp: Long,
    @SerialName("timezone")
    val timezone: String
) {
    @Serializable
    data class Geolocation(
        @SerialName("altitude")
        val altitude: Double,
        @SerialName("latitude")
        val latitude: Double,
        @SerialName("longitude")
        val longitude: Double
    )

    @Serializable
    data class Signature(
        @SerialName("samplems")
        val samplems: Long,
        @SerialName("timestamp")
        val timestamp: Long,
        @SerialName("uri")
        val uri: String
    )
}
