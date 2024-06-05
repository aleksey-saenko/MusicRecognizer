package com.mrsep.musicrecognizer.glue.recognition.adapters

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.data.track.TrackEntity
import com.mrsep.musicrecognizer.data.track.TrackRepositoryDo
import com.mrsep.musicrecognizer.feature.recognition.domain.TrackRepository
import com.mrsep.musicrecognizer.feature.recognition.domain.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AdapterTrackRepository @Inject constructor(
    private val trackRepositoryDo: TrackRepositoryDo,
    private val trackMapper: BidirectionalMapper<TrackEntity, Track>
) : TrackRepository {

    override fun getTrackFlow(trackId: String): Flow<Track?> {
        return trackRepositoryDo.getTrackFlow(trackId)
            .map { entity -> entity?.run(trackMapper::map) }
    }

    override suspend fun upsertKeepProperties(vararg tracks: Track): List<Track> {
        return trackRepositoryDo.upsertKeepProperties(
            *tracks.map(trackMapper::reverseMap).toTypedArray()
        ).map(trackMapper::map)
    }

    override suspend fun updateKeepProperties(vararg tracks: Track) {
        trackRepositoryDo.updateKeepProperties(
            *tracks.map(trackMapper::reverseMap).toTypedArray()
        )
    }

    override suspend fun setViewed(trackId: String, isViewed: Boolean) {
        trackRepositoryDo.setViewed(trackId, isViewed)
    }
}
