package com.mrsep.musicrecognizer.data.track

import com.mrsep.musicrecognizer.domain.model.Mapper
import com.mrsep.musicrecognizer.domain.model.Track
import java.time.LocalDate
import javax.inject.Inject

class TrackToDomainMapper @Inject constructor() : Mapper<TrackEntity, Track> {

    override fun map(input: TrackEntity): Track {
        return Track(
            mbId = input.mbId,
            title = input.title,
            artist = input.artist,
            releaseDate = input.releaseDate?.let { LocalDate.ofEpochDay(it) },
            album = input.album,
            lyrics = input.lyrics,
            links = Track.Links(
                artwork = input.links.artwork,
                spotify = input.links.spotify,
                youtube = input.links.youtube,
                appleMusic = input.links.appleMusic,
                musicBrainz = input.links.musicBrainz,
                deezer = input.links.deezer,
                napster = input.links.napster,
            ),
            metadata = Track.Metadata(
                lastRecognitionDate = input.metadata.lastRecognitionDate,
                isFavorite = input.metadata.isFavorite
            )
        )
    }

}