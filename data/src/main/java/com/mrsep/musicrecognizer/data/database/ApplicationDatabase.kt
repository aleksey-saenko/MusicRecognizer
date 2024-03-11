package com.mrsep.musicrecognizer.data.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mrsep.musicrecognizer.data.database.migration.AutoMigrationSpec3To4
import com.mrsep.musicrecognizer.data.enqueued.EnqueuedRecognitionDao
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionEntity
import com.mrsep.musicrecognizer.data.track.TrackDao
import com.mrsep.musicrecognizer.data.track.TrackEntity

@Database(
    entities = [
        TrackEntity::class,
        EnqueuedRecognitionEntity::class,
    ],
    version = 6,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4, spec = AutoMigrationSpec3To4::class),
        AutoMigration(from = 4, to = 5),
    ]
)
@TypeConverters(
    value = [
        FileRoomConverter::class,
        InstantRoomConverter::class,
        DurationRoomConverter::class,
        LocalDateRoomConverter::class,
    ]
)
internal abstract class ApplicationDatabase : RoomDatabase() {

    abstract fun trackDao(): TrackDao

    abstract fun enqueuedRecognitionDao(): EnqueuedRecognitionDao

}