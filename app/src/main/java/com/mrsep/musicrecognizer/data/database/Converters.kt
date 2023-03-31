package com.mrsep.musicrecognizer.data.database

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate

class Converters {

    @TypeConverter
    fun timestampToLocalDate(epochDay: Long?): LocalDate? {
        return epochDay?.let { LocalDate.ofEpochDay(epochDay) }
    }

    @TypeConverter
    fun localDateToTimestamp(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }

    @TypeConverter
    fun instantToTimestamp(instant: Instant): Long {
        return instant.epochSecond
    }

    @TypeConverter
    fun timestampToInstant(epochSecond: Long): Instant {
        return Instant.ofEpochSecond(epochSecond)
    }

}
