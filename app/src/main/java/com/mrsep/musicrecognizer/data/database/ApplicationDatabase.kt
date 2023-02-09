package com.mrsep.musicrecognizer.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mrsep.musicrecognizer.data.track.TrackDao
import com.mrsep.musicrecognizer.data.track.TrackEntity

@Database(entities = [TrackEntity::class], version = 1, exportSchema = true)
abstract class ApplicationDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
}