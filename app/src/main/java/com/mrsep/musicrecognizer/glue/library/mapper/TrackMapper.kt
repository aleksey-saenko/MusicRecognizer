package com.mrsep.musicrecognizer.glue.library.mapper

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.track.TrackPreview
import com.mrsep.musicrecognizer.feature.library.domain.model.Track
import javax.inject.Inject

class TrackMapper @Inject constructor() : Mapper<TrackPreview, Track> {

    override fun map(input: TrackPreview): Track {
        return Track(
            id = input.id,
            title = input.title,
            artist = input.artist,
            album = input.album,
            artworkThumbUrl = input.artworkThumbnail ?: input.artwork,
            recognitionDate = input.recognitionDate,
            isViewed = input.isViewed,
        )
    }
}
