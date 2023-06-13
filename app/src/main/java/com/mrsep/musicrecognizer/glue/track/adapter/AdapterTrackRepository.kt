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

    override suspend fun toggleFavoriteMark(mbId: String) {
        trackRepositoryDo.toggleFavoriteMark(mbId)
    }

    override suspend fun deleteByMbId(mbId: String) {
        trackRepositoryDo.deleteByMbId(mbId)
    }

    override fun getLyricsFlowById(mbId: String): Flow<String?> {
        return trackRepositoryDo.getLyricsFlowById(mbId)
    }

    override fun getByMbIdFlow(mbId: String): Flow<Track?> {
        return trackRepositoryDo.getByMbIdFlow(mbId)
            .map { entity -> entity?.let { trackMapper.map(it) } }
    }
}