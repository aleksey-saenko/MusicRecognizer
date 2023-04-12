package com.mrsep.musicrecognizer.data.database

import androidx.room.TypeConverter
import java.time.LocalDate

class LocalDateRoomConverter {

    @TypeConverter
    fun timestampToLocalDate(epochDay: Long?): LocalDate? {
        return epochDay?.let { LocalDate.ofEpochDay(epochDay) }
    }

    @TypeConverter
    fun localDateToTimestamp(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }

}