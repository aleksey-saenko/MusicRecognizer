package com.mrsep.musicrecognizer.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val Migration7To8 = object : Migration(7, 8) {

    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE track ADD COLUMN is_lyrics_synced INTEGER NOT NULL DEFAULT 0")
        db.execSQL("UPDATE track SET recognition_date = recognition_date * 1000")
    }
}
