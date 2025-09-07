package com.mrsep.musicrecognizer.core.database.migration

import androidx.room.RenameColumn
import androidx.room.migration.AutoMigrationSpec

@RenameColumn(
    tableName = "track",
    fromColumnName = "mb_id",
    toColumnName = "id"
)
@RenameColumn(
    tableName = "enqueued_recognition",
    fromColumnName = "result_mb_id",
    toColumnName = "result_track_id"
)
internal class AutoMigrationSpec3To4 : AutoMigrationSpec
