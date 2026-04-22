package com.mrsep.musicrecognizer.core.recognition.enhancer.youtube

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class YoutubeSearchRequestJson(
    @SerialName("context")
    val context: ContextJson,
    @SerialName("query")
    val query: String,
    @SerialName("params")
    val params: String?,
) {
    @Serializable
    internal data class ContextJson(
        @SerialName("client")
        val client: ClientJson,
        @SerialName("request")
        val request: RequestJson,
        @SerialName("user")
        val user: UserJson,
    )

    @Serializable
    internal data class ClientJson(
        @SerialName("clientName")
        val clientName: String,
        @SerialName("clientVersion")
        val clientVersion: String,
        @SerialName("hl")
        val hl: String,
        @SerialName("gl")
        val gl: String,
        @SerialName("originalUrl")
        val originalUrl: String?,
        @SerialName("platform")
        val platform: String,
        @SerialName("utcOffsetMinutes")
        val utcOffsetMinutes: Int,
    )

    @Serializable
    internal data class RequestJson(
        @SerialName("internalExperimentFlags")
        val internalExperimentFlags: List<String>,
        @SerialName("useSsl")
        val useSsl: Boolean,
    )

    @Serializable
    internal data class UserJson(
        @SerialName("lockedSafetyMode")
        val lockedSafetyMode: Boolean,
    )
}
