package com.mrsep.musicrecognizer.feature.recognition.domain.model

sealed class RecognitionServiceConfig

data class AuddConfig(
    val apiToken: String,
) : RecognitionServiceConfig()

data class AcrCloudConfig(
    val host: String,
    val accessKey: String,
    val accessSecret: String,
) : RecognitionServiceConfig()
