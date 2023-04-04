package com.mrsep.musicrecognizer.data.track

import com.mrsep.musicrecognizer.domain.model.Mapper
import com.mrsep.musicrecognizer.domain.model.Track
import javax.inject.Inject

class TrackToDataMapper @Inject constructor() : Mapper<Track, TrackEntity> {

    override fun map(input: Track): TrackEntity {
        return TrackEntity(
            mbId = input.mbId,
            title = input.title,
            artist = input.artist,
            releaseDate = input.releaseDate,
            album = input.album,
            lyrics = input.lyrics,
            links = TrackEntity.Links(
                artwork = input.links.artwork,
                spotify = input.links.spotify,
                youtube = input.links.youtube,
                soundCloud = input.links.soundCloud,
                appleMusic = input.links.appleMusic,
                musicBrainz = input.links.musicBrainz,
                deezer = input.links.deezer,
                napster = input.links.napster,
            ),
            metadata = TrackEntity.Metadata(
                lastRecognitionDate = input.metadata.lastRecognitionDate,
                isFavorite = input.metadata.isFavorite
            )
        )
    }

}