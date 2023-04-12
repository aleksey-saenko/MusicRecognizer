package com.mrsep.musicrecognizer.data.enqueued.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.File
import java.time.Instant

@Entity(tableName = "enqueued_recognition")
data class EnqueuedRecognitionEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "record_file")
    val recordFile: File,
    @ColumnInfo(name = "creation_date")
    val creationDate: Instant
)