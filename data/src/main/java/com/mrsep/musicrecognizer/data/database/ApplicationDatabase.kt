package com.mrsep.musicrecognizer.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mrsep.musicrecognizer.data.enqueued.EnqueuedRecognitionDao
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionEntity
import com.mrsep.musicrecognizer.data.track.TrackDao
import com.mrsep.musicrecognizer.data.track.TrackEntity

@Database(
    entities = [TrackEntity::class, EnqueuedRecognitionEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(
    value = [FileRoomConverter::class, InstantRoomConverter::class, LocalDateRoomConverter::class]
)
abstract class ApplicationDatabase : RoomDatabase() {

    abstract fun trackDao(): TrackDao

    abstract fun enqueuedRecognitionDao(): EnqueuedRecognitionDao

}