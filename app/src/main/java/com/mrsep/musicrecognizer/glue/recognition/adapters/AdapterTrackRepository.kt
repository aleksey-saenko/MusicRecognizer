package com.mrsep.musicrecognizer.glue.recognition.adapters

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.data.track.TrackRepositoryDo
import com.mrsep.musicrecognizer.data.track.TrackEntity
import com.mrsep.musicrecognizer.feature.recognition.domain.TrackRepository
import com.mrsep.musicrecognizer.feature.recognition.domain.model.Track
import javax.inject.Inject

class AdapterTrackRepository @Inject constructor(
    private val trackRepositoryDo: TrackRepositoryDo,
    private val trackMapper: BidirectionalMapper<TrackEntity, Track>
) : TrackRepository {

    override suspend fun insertOrReplace(vararg track: Track) {
        trackRepositoryDo.insertOrReplace(*track.map { trackMapper.reverseMap(it) }.toTypedArray())
    }

    override suspend fun insertOrReplaceSaveMetadata(vararg track: Track): List<Track> {
        return trackRepositoryDo.insertOrReplaceSaveMetadata(
            *track.map { trackMapper.reverseMap(it) }.toTypedArray()
        ).map { entity -> trackMapper.map(entity) }
    }

    override suspend fun getByMbId(mbId: String): Track? {
        return trackRepositoryDo.getByMbId(mbId)?.let { entity -> trackMapper.map(entity) }
    }

    override suspend fun update(track: Track) {
        trackRepositoryDo.update(trackMapper.reverseMap(track))
    }

}