package com.mrsep.musicrecognizer.glue.library.mapper

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.track.TrackEntity
import com.mrsep.musicrecognizer.feature.library.domain.model.Track
import javax.inject.Inject

class TrackMapper @Inject constructor() : Mapper<TrackEntity, Track> {

    override fun map(input: TrackEntity): Track {
        return Track(
            id = input.id,
            title = input.title,
            artist = input.artist,
            album = input.album,
            releaseDate = input.releaseDate,
            artworkThumbUrl = input.links.artworkThumbnail ?: input.links.artwork,
            recognitionDate = input.recognitionDate,
            isViewed = input.properties.isViewed,
            isFavorite = input.properties.isFavorite,
        )
    }
}
