package com.mrsep.musicrecognizer.data.track

import com.mrsep.musicrecognizer.data.database.ApplicationDatabase
import com.mrsep.musicrecognizer.domain.TrackRepository
import com.mrsep.musicrecognizer.domain.model.Mapper
import com.mrsep.musicrecognizer.domain.model.Track
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackRepositoryImpl @Inject constructor(
    private val trackToDomainMapper: Mapper<TrackEntity, Track>,
    private val trackToDataMapper: Mapper<Track, TrackEntity>,
    database: ApplicationDatabase
): TrackRepository {
    private val trackDao = database.trackDao()

    override suspend fun insertOrReplace(vararg track: Track) {
        trackDao.insertOrReplace(*track.map { trackToDataMapper.map(it) }.toTypedArray())
    }

    override suspend fun getUnique(mbId: String): Track? {
        return trackDao.getUnique(mbId)?.let { trackToDomainMapper.map(it) }
    }

    override suspend fun getAfterDate(date: Long, limit: Int): List<Track> {
        return trackDao.getAfterDate(date, limit).map { trackToDomainMapper.map(it) }
    }

    override suspend fun getLast(limit: Int): List<Track> {
        return trackDao.getLast(limit).map { trackToDomainMapper.map(it) }
    }

}