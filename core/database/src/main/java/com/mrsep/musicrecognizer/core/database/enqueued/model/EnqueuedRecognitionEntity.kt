package com.mrsep.musicrecognizer.core.database.enqueued.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mrsep.musicrecognizer.core.database.track.TrackEntity
import java.io.File
import java.time.Instant

@Entity(
    tableName = "enqueued_recognition",
    foreignKeys = [
        ForeignKey(
            entity = TrackEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("result_track_id"),
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["result_track_id"])]
)
data class EnqueuedRecognitionEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "record_file")
    val recordFile: File,
    @ColumnInfo(name = "creation_date")
    val creationDate: Instant,

    // result can't be embedded object due to constraint of foreign key indexing, see Index docs
    @ColumnInfo(name = "result_type")
    val resultType: RemoteRecognitionResultType? = null,
    @ColumnInfo(name = "result_track_id")
    val resultTrackId: String? = null,
    @ColumnInfo(name = "result_message")
    val resultMessage: String? = null,
    @ColumnInfo(name = "result_date")
    val resultDate: Instant? = null
)
