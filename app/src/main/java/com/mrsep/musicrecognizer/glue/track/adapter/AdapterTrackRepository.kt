package com.mrsep.musicrecognizer.glue.track.adapter

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.track.TrackRepositoryDo
import com.mrsep.musicrecognizer.data.track.TrackEntity
import com.mrsep.musicrecognizer.feature.track.domain.TrackRepository
import com.mrsep.musicrecognizer.feature.track.domain.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AdapterTrackRepository @Inject constructor(
    private val trackRepositoryDo: TrackRepositoryDo,
    private val trackMapper: Mapper<TrackEntity, Track>
) : TrackRepository {

    override fun getTrackFlow(trackId: String): Flow<Track?> {
        return trackRepositoryDo.getTrackFlow(trackId)
            .map { entity -> entity?.run(trackMapper::map) }
    }

    override suspend fun setFavorite(trackId: String, isFavorite: Boolean) {
        trackRepositoryDo.setFavorite(trackId, isFavorite)
    }

    override suspend fun delete(trackId: String) {
        trackRepositoryDo.delete(trackId)
    }

    override suspend fun setThemeSeedColor(trackId: String, color: Int?) {
        trackRepositoryDo.setThemeSeedColor(trackId, color)
    }

    override suspend fun setAsViewed(trackId: String) {
        trackRepositoryDo.setAsViewed(trackId)
    }

}