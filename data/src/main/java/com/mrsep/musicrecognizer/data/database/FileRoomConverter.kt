package com.mrsep.musicrecognizer.data.database

import androidx.room.TypeConverter
import java.io.File

class FileRoomConverter {

    @TypeConverter
    fun stringToFile(filepath: String): File {
        return File(filepath)
    }

    @TypeConverter
    fun fileToString(file: File): String {
        return file.absolutePath
    }

}