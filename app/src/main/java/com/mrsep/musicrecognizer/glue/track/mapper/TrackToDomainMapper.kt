package com.mrsep.musicrecognizer.glue.track.mapper

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.track.TrackEntity
import com.mrsep.musicrecognizer.feature.track.domain.model.Track
import javax.inject.Inject

class TrackToDomainMapper @Inject constructor() : Mapper<TrackEntity, Track> {

    override fun map(input: TrackEntity): Track {
        return Track(
            mbId = input.mbId,
            title = input.title,
            artist = input.artist,
            album = input.album,
            releaseDate = input.releaseDate,
            lyrics = input.lyrics,
            links = Track.Links(
                artwork = input.links.artwork,
                spotify = input.links.spotify,
                youtube = input.links.youtube,
                soundCloud = input.links.soundCloud,
                appleMusic = input.links.appleMusic,
                musicBrainz = input.links.musicBrainz,
                deezer = input.links.deezer,
                napster = input.links.napster
            ),
            metadata = Track.Metadata(
                lastRecognitionDate = input.metadata.lastRecognitionDate,
                isFavorite = input.metadata.isFavorite
            )
        )
    }

}