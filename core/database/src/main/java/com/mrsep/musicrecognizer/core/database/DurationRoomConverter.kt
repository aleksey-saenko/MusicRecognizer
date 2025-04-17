package com.mrsep.musicrecognizer.core.database

import androidx.room.TypeConverter
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

internal class DurationRoomConverter {

    @TypeConverter
    fun durationToTimestamp(duration: Duration): Long {
        return duration.inWholeMilliseconds
    }

    @TypeConverter
    fun timestampToDuration(millis: Long): Duration {
        return millis.milliseconds
    }
}
