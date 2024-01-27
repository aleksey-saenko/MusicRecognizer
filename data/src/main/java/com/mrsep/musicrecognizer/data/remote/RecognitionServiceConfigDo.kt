package com.mrsep.musicrecognizer.data.remote

sealed class RecognitionServiceConfigDo

data class AuddConfigDo(
    val apiToken: String,
) : RecognitionServiceConfigDo()

data class AcrCloudConfigDo(
    val host: String,
    val accessKey: String,
    val accessSecret: String,
) : RecognitionServiceConfigDo()

//data class AudioTagConfigDo(
//    val apiKey: String,
//) : RecognitionServiceConfigDo()