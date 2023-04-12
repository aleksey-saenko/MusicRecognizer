package com.mrsep.musicrecognizer.glue.track.mapper

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.track.TrackEntity
import com.mrsep.musicrecognizer.feature.track.domain.model.Track
import javax.inject.Inject

class TrackToDataMapper @Inject constructor() : Mapper<Track, TrackEntity> {

    override fun map(input: Track): TrackEntity {
        return TrackEntity(
            mbId = input.mbId,
            title = input.title,
            artist = input.artist,
            album = input.album,
            releaseDate = input.releaseDate,
            lyrics = input.lyrics,
            links = TrackEntity.Links(
                artwork = input.links.artwork,
                spotify = input.links.spotify,
                youtube = input.links.youtube,
                soundCloud = input.links.soundCloud,
                appleMusic = input.links.appleMusic,
                musicBrainz = input.links.musicBrainz,
                deezer = input.links.deezer,
                napster = input.links.napster
            ),
            metadata = TrackEntity.Metadata(
                lastRecognitionDate = input.metadata.lastRecognitionDate,
                isFavorite = input.metadata.isFavorite
            )
        )
    }

}