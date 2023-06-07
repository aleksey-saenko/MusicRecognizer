package com.mrsep.musicrecognizer.glue.track.adapter

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.data.track.TrackRepositoryDo
import com.mrsep.musicrecognizer.data.track.TrackEntity
import com.mrsep.musicrecognizer.feature.track.domain.TrackRepository
import com.mrsep.musicrecognizer.feature.track.domain.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AdapterTrackRepository @Inject constructor(
    private val trackRepositoryDo: TrackRepositoryDo,
    private val trackMapper: BidirectionalMapper<TrackEntity, Track>
) : TrackRepository {

    override suspend fun update(track: Track) {
        trackRepositoryDo.update(trackMapper.reverseMap(track))
    }

    override fun getByMbIdFlow(mbId: String): Flow<Track?> {
        return trackRepositoryDo.getByMbIdFlow(mbId)
            .map { entity -> entity?.let { trackMapper.map(it) } }
    }
}