package com.mrsep.musicrecognizer.data.track

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.mrsep.musicrecognizer.data.database.ApplicationDatabase
import com.mrsep.musicrecognizer.di.DefaultDispatcher
import com.mrsep.musicrecognizer.di.IoDispatcher
import com.mrsep.musicrecognizer.domain.SearchResult
import com.mrsep.musicrecognizer.domain.TrackRepository
import com.mrsep.musicrecognizer.domain.model.Mapper
import com.mrsep.musicrecognizer.domain.model.Track
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackRepositoryImpl @Inject constructor(
    private val trackToDomainMapper: Mapper<TrackEntity, Track>,
    private val trackToDataMapper: Mapper<Track, TrackEntity>,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    database: ApplicationDatabase
) : TrackRepository {
    private val trackDao = database.trackDao()

    override fun getPagedFlow(): Flow<PagingData<Track>> {
        return Pager(
            config = PagingConfig(
                pageSize = TRACK_PAGE_SIZE,
                enablePlaceholders = false,
//                maxSize = 100
            ),
            pagingSourceFactory = {
                trackDao.pagingSource()
//                TrackPagingSource(
//                    loadTracks = ::getWithOffset,
//                    TRACK_PAGE_SIZE
//                )
            }
        ).flow
            .map { pagingData -> pagingData.map { entity -> trackToDomainMapper.map(entity) } }
            .flowOn(ioDispatcher)
    }

    private suspend fun getWithOffset(pageIndex: Int, pageSize: Int): List<Track>  {
        return withContext(ioDispatcher) {
            trackDao.getWihOffset(limit = pageSize, offset = pageIndex * pageSize)
                .map { trackToDomainMapper.map(it) }
        }
    }


    override suspend fun insertOrReplace(vararg track: Track) {
        withContext(ioDispatcher) {
            trackDao.insertOrReplace(*track.map { trackToDataMapper.map(it) }.toTypedArray())
        }
    }

    override suspend fun insertOrReplaceSaveMetadata(vararg track: Track): List<Track> {
        return withContext(ioDispatcher) {
            val trackList = track.map { newTrack ->
                getByMbId(newTrack.mbId)?.run {
                    newTrack.copy(
                        metadata = newTrack.metadata.copy(
                            isFavorite = this.metadata.isFavorite
                        )
                    )
                } ?: newTrack
            }
            trackDao.insertOrReplace(*trackList.map { trackToDataMapper.map(it) }.toTypedArray())
            trackList
        }
    }

    override suspend fun update(track: Track) {
        withContext(ioDispatcher) {
            trackDao.update(trackToDataMapper.map(track))
        }
    }

    override suspend fun delete(vararg track: Track) {
        withContext(ioDispatcher) {
            trackDao.delete(*track.map { trackToDataMapper.map(it) }.toTypedArray())
        }
    }

    override suspend fun deleteAll() {
        withContext(ioDispatcher) {
            trackDao.deleteAll()
        }
    }

    override suspend fun deleteAllExceptFavorites() {
        withContext(ioDispatcher) {
            trackDao.deleteAllExceptFavorites()
        }
    }

    override suspend fun deleteAllFavorites() {
        withContext(ioDispatcher) {
            trackDao.deleteAllFavorites()
        }
    }

    override suspend fun getByMbId(mbId: String): Track? {
        return withContext(ioDispatcher) {
            trackDao.getByMbId(mbId)?.let { trackToDomainMapper.map(it) }
        }
    }

    override fun getByMbIdFlow(mbId: String): Flow<Track?> {
        return trackDao.getByMbIdFlow(mbId)
            .map { track -> track?.let { trackToDomainMapper.map(it) } }
            .flowOn(ioDispatcher)

    }

    override suspend fun getAfterDate(date: Long, limit: Int): List<Track> {
        return withContext(ioDispatcher) {
            trackDao.getAfterDate(date, limit).map { trackToDomainMapper.map(it) }
        }
    }

    override suspend fun getLastRecognized(limit: Int): List<Track> {
        return withContext(ioDispatcher) {
            trackDao.getLastRecognized(limit).map { trackToDomainMapper.map(it) }
        }
    }

    override fun getLastRecognizedFlow(limit: Int): Flow<List<Track>> {
        return trackDao.getLastRecognizedFlow(limit)
            .map { list -> list.map { trackEntity -> trackToDomainMapper.map(trackEntity) } }
            .flowOn(ioDispatcher)
    }

    override fun getFavoritesFlow(limit: Int): Flow<List<Track>> {
        return trackDao.getFavoritesFlow(limit)
            .map { list -> list.map { trackEntity -> trackToDomainMapper.map(trackEntity) } }
            .flowOn(ioDispatcher)
    }

    override suspend fun search(keyword: String, limit: Int): List<Track> {
        return withContext(ioDispatcher) {
            val searchKey = createSearchKeyForSQLite(keyword)
            trackDao.search(searchKey, ESCAPE_SYMBOL, limit).map { trackToDomainMapper.map(it) }
        }
    }

    override fun searchFlow(keyword: String, limit: Int): Flow<List<Track>> {
        val searchKey = createSearchKeyForSQLite(keyword)
        return trackDao.searchFlow(searchKey, ESCAPE_SYMBOL, limit)
            .map { list -> list.map { trackEntity -> trackToDomainMapper.map(trackEntity) } }
            .flowOn(ioDispatcher)
    }

    override fun searchResultFlow(keyword: String, limit: Int): Flow<SearchResult<Track>> {
        val searchKey = createSearchKeyForSQLite(keyword)
        return trackDao.searchFlow(searchKey, ESCAPE_SYMBOL, limit)
            .map<List<TrackEntity>, SearchResult<Track>> { list ->
//                delay(3000) //debug purpose
                SearchResult.Success(
                    keyword = keyword,
                    data = list.map { trackEntity -> trackToDomainMapper.map(trackEntity) }
                )
            }
            .onStart { emit(SearchResult.Processing(keyword)) }
            .flowOn(ioDispatcher)
    }

    private fun createSearchKeyForSQLite(word: String): String {
        return "%" + word.replace("%", "${ESCAPE_SYMBOL}%")
            .replace("_", "${ESCAPE_SYMBOL}_") + "%"
    }

    companion object {
        private const val ESCAPE_SYMBOL = "/"
        private const val TRACK_PAGE_SIZE = 30
    }

}