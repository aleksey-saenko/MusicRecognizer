package com.mrsep.musicrecognizer.data.enqueued.model

import androidx.room.Embedded
import androidx.room.Relation
import com.mrsep.musicrecognizer.data.track.TrackEntity

data class EnqueuedRecognitionEntityWithTrack(
    @Embedded val enqueued: EnqueuedRecognitionEntity,
    @Relation(
        parentColumn = "result_mb_id",
        entityColumn = "mb_id"
    )
    val track: TrackEntity?
)