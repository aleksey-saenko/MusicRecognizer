package com.mrsep.musicrecognizer.core.database

import androidx.room.TypeConverter
import java.io.File

internal class FileRoomConverter {

    @TypeConverter
    fun stringToFile(filepath: String): File {
        return File(filepath)
    }

    @TypeConverter
    fun fileToString(file: File): String {
        return file.absolutePath
    }
}
