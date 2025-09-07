package com.mrsep.musicrecognizer.core.database

import androidx.room.TypeConverter
import java.time.Instant

internal class InstantRoomConverter {

    @TypeConverter
    fun instantToTimestamp(instant: Instant): Long {
        return instant.toEpochMilli()
    }

    @TypeConverter
    fun timestampToInstant(epochMillis: Long): Instant {
        return Instant.ofEpochMilli(epochMillis)
    }
}
