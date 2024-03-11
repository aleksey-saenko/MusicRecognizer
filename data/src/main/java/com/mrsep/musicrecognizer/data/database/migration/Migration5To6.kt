package com.mrsep.musicrecognizer.data.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val Migration5To6 = object : Migration(5, 6) {

    override fun migrate(db: SupportSQLiteDatabase) {
        migrateTrackTable(db)
        migrateEnqueuedRecognitionTable(db)
    }

    private fun migrateTrackTable(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE track RENAME COLUMN last_recognition_date TO recognition_date")
        db.execSQL("ALTER TABLE track ADD COLUMN recognized_by TEXT NOT NULL DEFAULT 'Audd'")
        db.execSQL("ALTER TABLE track ADD COLUMN is_viewed INTEGER NOT NULL DEFAULT 1")
        db.execSQL("CREATE INDEX index_track_is_viewed ON track (is_viewed)")
    }

    private fun migrateEnqueuedRecognitionTable(db: SupportSQLiteDatabase) {
        db.execSQL("""
            UPDATE enqueued_recognition
            SET result_type = CASE 
                WHEN result_type = 'WrongToken' THEN 'AuthError' 
                WHEN result_type = 'LimitedToken' THEN 'ApiUsageLimited' 
                ELSE result_type
            END
        """)
    }

}