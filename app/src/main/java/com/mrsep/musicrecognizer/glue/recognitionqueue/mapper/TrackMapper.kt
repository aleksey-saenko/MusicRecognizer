package com.mrsep.musicrecognizer.glue.recognitionqueue.mapper

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.track.TrackEntity
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.model.Track
import javax.inject.Inject


class TrackMapper @Inject constructor() : Mapper<TrackEntity, Track> {

    override fun map(input: TrackEntity): Track {
        return Track(mbId = input.mbId)
    }

}