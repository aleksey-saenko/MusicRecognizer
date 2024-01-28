package com.mrsep.musicrecognizer.data.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mrsep.musicrecognizer.data.database.migration.MigrationSpec3To4RenameTrackId
import com.mrsep.musicrecognizer.data.enqueued.EnqueuedRecognitionDao
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionEntity
import com.mrsep.musicrecognizer.data.track.TrackDao
import com.mrsep.musicrecognizer.data.track.TrackEntity

@Database(
    entities = [
        TrackEntity::class,
        EnqueuedRecognitionEntity::class,
    ],
    version = 4,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4, spec = MigrationSpec3To4RenameTrackId::class),
    ]
)
@TypeConverters(
    value = [
        FileRoomConverter::class,
        InstantRoomConverter::class,
        LocalDateRoomConverter::class,
    ]
)
abstract class ApplicationDatabase : RoomDatabase() {

    abstract fun trackDao(): TrackDao

    abstract fun enqueuedRecognitionDao(): EnqueuedRecognitionDao

}