package com.mrsep.musicrecognizer.core.database

import androidx.room.TypeConverter
import java.time.Instant

internal class InstantRoomConverter {

    @TypeConverter
    fun instantToTimestamp(instant: Instant): Long {
        return instant.epochSecond
    }

    @TypeConverter
    fun timestampToInstant(epochSecond: Long): Instant {
        return Instant.ofEpochSecond(epochSecond)
    }
}
