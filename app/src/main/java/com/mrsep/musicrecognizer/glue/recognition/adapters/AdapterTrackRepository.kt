package com.mrsep.musicrecognizer.glue.recognition.adapters

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.track.TrackDataRepository
import com.mrsep.musicrecognizer.data.track.TrackEntity
import com.mrsep.musicrecognizer.feature.recognition.domain.TrackRepository
import com.mrsep.musicrecognizer.feature.recognition.domain.model.Track
import javax.inject.Inject

class AdapterTrackRepository @Inject constructor(
    private val trackDataRepository: TrackDataRepository,
    private val trackToDomainMapper: Mapper<TrackEntity, Track>,
    private val trackToDataMapper: Mapper<Track, TrackEntity>
) : TrackRepository {

    override suspend fun insertOrReplace(vararg track: Track) {
        trackDataRepository.insertOrReplace(*track.map { trackToDataMapper.map(it) }.toTypedArray())
    }

    override suspend fun insertOrReplaceSaveMetadata(vararg track: Track): List<Track> {
        return trackDataRepository.insertOrReplaceSaveMetadata(
            *track.map { trackToDataMapper.map(it) }.toTypedArray()
        ).map { entity -> trackToDomainMapper.map(entity) }
    }

    override suspend fun getByMbId(mbId: String): Track? {
        return trackDataRepository.getByMbId(mbId)?.let { entity -> trackToDomainMapper.map(entity) }
    }

    override suspend fun update(track: Track) {
        trackDataRepository.update(trackToDataMapper.map(track))
    }

}