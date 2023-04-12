package com.mrsep.musicrecognizer.data.track

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.mrsep.musicrecognizer.core.common.di.DefaultDispatcher
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.data.database.ApplicationDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TrackRepositoryImpl @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    database: ApplicationDatabase
) : TrackDataRepository {
    private val trackDao = database.trackDao()

    override fun getPagedFlow(): Flow<PagingData<TrackEntity>> {
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
//            .map { pagingData -> pagingData.map { entity -> trackToDomainMapper.map(entity) } }
            .flowOn(ioDispatcher)
    }

    override suspend fun getWithOffset(pageIndex: Int, pageSize: Int): List<TrackEntity>  {
        return withContext(ioDispatcher) {
            trackDao.getWihOffset(limit = pageSize, offset = pageIndex * pageSize)
//                .map { trackToDomainMapper.map(it) }
        }
    }


    override suspend fun insertOrReplace(vararg track: TrackEntity) {
        withContext(ioDispatcher) {
            trackDao.insertOrReplace(*track)
        }
    }

    override suspend fun insertOrReplaceSaveMetadata(vararg track: TrackEntity): List<TrackEntity> {
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
            trackDao.insertOrReplace(*trackList.toTypedArray())
            trackList
        }
    }

    override suspend fun update(track: TrackEntity) {
        withContext(ioDispatcher) {
            trackDao.update(track)
        }
    }

    override suspend fun delete(vararg track: TrackEntity) {
        withContext(ioDispatcher) {
            trackDao.delete(*track)
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

    override suspend fun getByMbId(mbId: String): TrackEntity? {
        return withContext(ioDispatcher) {
            trackDao.getByMbId(mbId)
        }
    }

    override fun getByMbIdFlow(mbId: String): Flow<TrackEntity?> {
        return trackDao.getByMbIdFlow(mbId)
            .flowOn(ioDispatcher)

    }

    override suspend fun getAfterDate(date: Long, limit: Int): List<TrackEntity> {
        return withContext(ioDispatcher) {
            trackDao.getAfterDate(date, limit)
        }
    }

    override suspend fun getLastRecognized(limit: Int): List<TrackEntity> {
        return withContext(ioDispatcher) {
            trackDao.getLastRecognized(limit)
        }
    }

    override fun getLastRecognizedFlow(limit: Int): Flow<List<TrackEntity>> {
        return trackDao.getLastRecognizedFlow(limit)
            .flowOn(ioDispatcher)
    }

    override fun getFavoritesFlow(limit: Int): Flow<List<TrackEntity>> {
        return trackDao.getFavoritesFlow(limit)
            .flowOn(ioDispatcher)
    }

    override suspend fun search(keyword: String, limit: Int): List<TrackEntity> {
        return withContext(ioDispatcher) {
            val searchKey = createSearchKeyForSQLite(keyword)
            trackDao.search(searchKey, ESCAPE_SYMBOL, limit)
        }
    }

    override fun searchFlow(keyword: String, limit: Int): Flow<List<TrackEntity>> {
        val searchKey = createSearchKeyForSQLite(keyword)
        return trackDao.searchFlow(searchKey, ESCAPE_SYMBOL, limit)
            .flowOn(ioDispatcher)
    }

    override fun searchResultFlow(keyword: String, limit: Int): Flow<SearchDataResult<TrackEntity>> {
        val searchKey = createSearchKeyForSQLite(keyword)
        return trackDao.searchFlow(searchKey, ESCAPE_SYMBOL, limit)
            .map<List<TrackEntity>, SearchDataResult<TrackEntity>> { list ->
//                delay(3000) //debug purpose
                SearchDataResult.Success(
                    keyword = keyword,
                    data = list
                )
            }
            .onStart { emit(SearchDataResult.Processing(keyword)) }
            .flowOn(ioDispatcher)
    }

    override fun createSearchKeyForSQLite(word: String): String {
        return "%" + word.replace("%", "${ESCAPE_SYMBOL}%")
            .replace("_", "${ESCAPE_SYMBOL}_") + "%"
    }

    companion object {
        private const val ESCAPE_SYMBOL = "/"
        private const val TRACK_PAGE_SIZE = 30
    }

}