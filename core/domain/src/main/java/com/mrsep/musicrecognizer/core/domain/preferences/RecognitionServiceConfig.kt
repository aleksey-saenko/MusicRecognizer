package com.mrsep.musicrecognizer.core.domain.preferences

sealed class RecognitionServiceConfig

data class AuddConfig(
    val apiToken: String,
) : RecognitionServiceConfig()

data class AcrCloudConfig(
    val host: String,
    val accessKey: String,
    val accessSecret: String,
) : RecognitionServiceConfig()
