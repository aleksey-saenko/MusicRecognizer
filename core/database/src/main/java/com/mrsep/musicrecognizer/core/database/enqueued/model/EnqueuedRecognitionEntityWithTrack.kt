package com.mrsep.musicrecognizer.core.database.enqueued.model

import androidx.room.Embedded
import androidx.room.Relation
import com.mrsep.musicrecognizer.core.database.track.TrackEntity

data class EnqueuedRecognitionEntityWithTrack(
    @Embedded val enqueued: EnqueuedRecognitionEntity,
    @Relation(
        parentColumn = "result_track_id",
        entityColumn = "id"
    )
    val track: TrackEntity?
)
