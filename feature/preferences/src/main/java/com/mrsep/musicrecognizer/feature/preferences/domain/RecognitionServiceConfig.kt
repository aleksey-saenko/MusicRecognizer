package com.mrsep.musicrecognizer.feature.preferences.domain

data class AuddConfig(
    val apiToken: String,
)

data class AcrCloudConfig(
    val host: String,
    val accessKey: String,
    val accessSecret: String,
)