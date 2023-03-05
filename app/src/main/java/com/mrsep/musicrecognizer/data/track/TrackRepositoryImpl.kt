package com.mrsep.musicrecognizer.data.track

import com.mrsep.musicrecognizer.data.database.ApplicationDatabase
import com.mrsep.musicrecognizer.di.IoDispatcher
import com.mrsep.musicrecognizer.domain.TrackRepository
import com.mrsep.musicrecognizer.domain.model.Mapper
import com.mrsep.musicrecognizer.domain.model.Track
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackRepositoryImpl @Inject constructor(
    private val trackToDomainMapper: Mapper<TrackEntity, Track>,
    private val trackToDataMapper: Mapper<Track, TrackEntity>,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    database: ApplicationDatabase
) : TrackRepository {
    private val trackDao = database.trackDao()

    override suspend fun insertOrReplace(vararg track: Track) {
        trackDao.insertOrReplace(*track.map { trackToDataMapper.map(it) }.toTypedArray())
    }

    override suspend fun update(track: Track) {
        trackDao.update(trackToDataMapper.map(track))
    }

    override suspend fun delete(vararg track: Track) {
        trackDao.delete(*track.map { trackToDataMapper.map(it) }.toTypedArray())
    }

    override suspend fun deleteAll() {
        trackDao.deleteAll()
    }

    override suspend fun deleteAllExceptFavorites() {
        trackDao.deleteAllExceptFavorites()
    }

    override suspend fun deleteAllFavorites() {
        trackDao.deleteAllFavorites()
    }

    override suspend fun getByMbId(mbId: String): Track? {
        return trackDao.getByMbId(mbId)?.let { trackToDomainMapper.map(it) }
    }

    override fun getByMbIdFlow(mbId: String): Flow<Track?> {
        return trackDao.getByMbIdFlow(mbId)
            .map { track -> track?.let { trackToDomainMapper.map(it) } }
    }

    override suspend fun getAfterDate(date: Long, limit: Int): List<Track> {
        return trackDao.getAfterDate(date, limit).map { trackToDomainMapper.map(it) }
    }

    override suspend fun getLastRecognized(limit: Int): List<Track> {
        return trackDao.getLastRecognized(limit).map { trackToDomainMapper.map(it) }
    }

    override fun getLastRecognizedFlow(limit: Int): Flow<List<Track>> {
        return trackDao.getLastRecognizedFlow(limit)
            .map { list -> list.map { trackEntity -> trackToDomainMapper.map(trackEntity) } }
    }

    override fun getFavoritesFlow(limit: Int): Flow<List<Track>> {
        return trackDao.getFavoritesFlow(limit)
            .map { list -> list.map { trackEntity -> trackToDomainMapper.map(trackEntity) } }
    }

    override suspend fun search(keyword: String, limit: Int): List<Track> {
        return withContext(ioDispatcher) {
            val escapeSymbol = "/"
            val searchKey = createSearchKeyForSQLite(keyword, escapeSymbol)
            trackDao.search(searchKey, escapeSymbol, limit).map { trackToDomainMapper.map(it) }
        }
    }

    override fun searchFlow(keyword: String, limit: Int): Flow<List<Track>> {
        val escapeSymbol = "/"
        val searchKey = createSearchKeyForSQLite(keyword, escapeSymbol)
        return trackDao.searchFlow(searchKey, escapeSymbol, limit)
            .map { list -> list.map { trackEntity -> trackToDomainMapper.map(trackEntity) } }
    }

    private fun createSearchKeyForSQLite(word: String, escapeSymbol: String): String {
        return "%" + word.replace("%", "${escapeSymbol}%")
            .replace("_", "${escapeSymbol}_") + "%"
    }

}