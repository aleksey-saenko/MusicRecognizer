package com.mrsep.musicrecognizer.core.database

import androidx.room.TypeConverter
import java.time.LocalDate

internal class LocalDateRoomConverter {

    @TypeConverter
    fun timestampToLocalDate(epochDay: Long): LocalDate {
        return LocalDate.ofEpochDay(epochDay)
    }

    @TypeConverter
    fun localDateToTimestamp(date: LocalDate): Long {
        return date.toEpochDay()
    }
}
