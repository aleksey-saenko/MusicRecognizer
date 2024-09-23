package com.mrsep.musicrecognizer.data.database

import android.util.Log
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SimpleSQLiteQuery
import com.mrsep.musicrecognizer.data.database.migration.AutoMigrationSpec3To4
import com.mrsep.musicrecognizer.data.enqueued.EnqueuedRecognitionDao
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionEntity
import com.mrsep.musicrecognizer.data.track.TrackDao
import com.mrsep.musicrecognizer.data.track.TrackEntity
import kotlinx.coroutines.delay

@Database(
    entities = [
        TrackEntity::class,
        EnqueuedRecognitionEntity::class,
    ],
    version = 7,
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

    fun getDataSize(): Long {
        val q = "SELECT page_count * page_size as size FROM pragma_page_count(), pragma_page_size()"
        return query(SimpleSQLiteQuery(q)).run {
            moveToFirst()
            getLong(0)
        }
    }

    suspend fun checkoutWithRetry(): Boolean {
        var attemptCount = 1
        while (attemptCount <= 3) {
            if (checkout()) return true
            Log.i(this::class.simpleName, "Database checkpoint was blocked, retry")
            delay(500L * attemptCount)
            attemptCount++
        }
        return false
    }

    // https://www.sqlite.org/pragma.html#pragma_wal_checkpoint
    private fun checkout(): Boolean {
        val cursor = query(SimpleSQLiteQuery("PRAGMA wal_checkpoint(FULL)"))
        cursor.moveToFirst()
        return if (cursor.getInt(0) == 0) {
            if (cursor.getInt(1) == -1 && cursor.getInt(2) == -1) {
                Log.w(this::class.simpleName, "There is no write-ahead log for database")
            }
            true
        } else {
            false
        }
    }
}
