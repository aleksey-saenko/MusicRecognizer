package com.mrsep.musicrecognizer.data.database

import androidx.room.TypeConverter
import java.time.Instant

class InstantRoomConverter {

    @TypeConverter
    fun instantToTimestamp(instant: Instant): Long {
        return instant.epochSecond
    }

    @TypeConverter
    fun timestampToInstant(epochSecond: Long): Instant {
        return Instant.ofEpochSecond(epochSecond)
    }

}