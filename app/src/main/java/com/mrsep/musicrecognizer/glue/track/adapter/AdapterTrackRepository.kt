package com.mrsep.musicrecognizer.glue.track.adapter

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.track.TrackDataRepository
import com.mrsep.musicrecognizer.data.track.TrackEntity
import com.mrsep.musicrecognizer.feature.track.domain.TrackRepository
import com.mrsep.musicrecognizer.feature.track.domain.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AdapterTrackRepository @Inject constructor(
    private val trackDataRepository: TrackDataRepository,
    private val trackToDomainMapper: Mapper<TrackEntity, Track>,
    private val trackToDataMapper: Mapper<Track, TrackEntity>,
) : TrackRepository {

    override suspend fun update(track: Track) {
        trackDataRepository.update(trackToDataMapper.map(track))
    }

    override fun getByMbIdFlow(mbId: String): Flow<Track?> {
        return trackDataRepository.getByMbIdFlow(mbId)
            .map { entity -> entity?.let { trackToDomainMapper.map(it) } }
    }
}