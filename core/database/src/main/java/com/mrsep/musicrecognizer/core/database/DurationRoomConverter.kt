package com.mrsep.musicrecognizer.core.database

import androidx.room.TypeConverter
import java.time.Duration

internal class DurationRoomConverter {

    @TypeConverter
    fun durationToTimestamp(duration: Duration): Long {
        return duration.toMillis()
    }

    @TypeConverter
    fun timestampToDuration(millis: Long): Duration {
        return Duration.ofMillis(millis)
    }
}
