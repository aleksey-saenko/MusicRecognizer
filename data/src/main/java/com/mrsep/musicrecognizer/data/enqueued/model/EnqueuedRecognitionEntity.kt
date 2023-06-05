package com.mrsep.musicrecognizer.data.enqueued.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.mrsep.musicrecognizer.data.track.TrackEntity
import java.io.File
import java.time.Instant

@Entity(
    tableName = "enqueued_recognition",
    foreignKeys = [ForeignKey(
        entity = TrackEntity::class,
        parentColumns = arrayOf("mb_id"),
        childColumns = arrayOf("result_mb_id"),
        onDelete = ForeignKey.SET_NULL,
        onUpdate = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["result_mb_id"])]
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

    // result can't be embedded object because of indexing foreign key, see Index docs
    @ColumnInfo(name = "result_type")
    val resultType: RemoteRecognitionResultType? = null,
    @ColumnInfo(name = "result_mb_id")
    val resultMbId: String? = null,
    @ColumnInfo(name = "result_message")
    val resultMessage: String? = null,
    @ColumnInfo(name = "result_date")
    val resultDate: Instant? = null
)


data class EnqueuedWithOptionalTrack(
    @Embedded val enqueued: EnqueuedRecognitionEntity,
    @Relation(
         parentColumn = "result_mb_id",
         entityColumn = "mb_id"
    )
    val track: TrackEntity?
)

enum class RemoteRecognitionResultType {
    Success,
    NoMatches,
    BadConnection,
    BadRecording,
    WrongToken,
    LimitedToken,
    HttpError,
    UnhandledError
}