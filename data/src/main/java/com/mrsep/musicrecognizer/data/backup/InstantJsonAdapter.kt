package com.mrsep.musicrecognizer.data.backup

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.Instant

internal class InstantJsonAdapter {
    @ToJson
    fun toJson(instant: Instant): Long = instant.epochSecond

    @FromJson
    fun fromJson(epochSecond: Long): Instant = Instant.ofEpochSecond(epochSecond)
}