package com.mrsep.musicrecognizer.feature.onboarding.domain.model

sealed class RecognitionServiceConfig

data class AuddConfig(
    val apiToken: String,
) : RecognitionServiceConfig()

data class AcrCloudConfig(
    val host: String,
    val accessKey: String,
    val accessSecret: String,
) : RecognitionServiceConfig()
