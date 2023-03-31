package com.mrsep.musicrecognizer.data.enqueued

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "enqueued_recognition")
data class EnqueuedRecognitionEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "filepath")
    val filepath: String,
    @ColumnInfo(name = "creation_date")
    val creationDate: Instant
)