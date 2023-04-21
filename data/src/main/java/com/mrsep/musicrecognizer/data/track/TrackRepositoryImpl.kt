package com.mrsep.musicrecognizer.data.track

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
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

    override fun isEmptyFlow(): Flow<Boolean> {
        return trackDao.isEmptyFlow()
            .distinctUntilChanged()
            .flowOn(ioDispatcher)
    }

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

    override fun countAllFlow(): Flow<Int> {
        return trackDao.countAllFlow()
            .flowOn(ioDispatcher)
    }

    override fun countFavoritesFlow(): Flow<Int> {
        return trackDao.countFavoritesFlow()
            .flowOn(ioDispatcher)
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

    override fun getFilteredFlow(filter: TrackDataFilter): Flow<List<TrackEntity>> {
        return trackDao.getFlowByCustomQuery(filter.toSQLiteQuery())
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

    override fun getNotFavoriteRecentsFlow(limit: Int): Flow<List<TrackEntity>> {
        return trackDao.getNotFavoriteRecentsFlow(limit)
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
            .onStart { emit(SearchDataResult.Pending(keyword)) }
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

private fun TrackDataFilter.toSQLiteQuery(): SupportSQLiteQuery {
    val builder = StringBuilder("SELECT * FROM track")
    val params = mutableListOf<Any>()
    var whereUsed = false
    when (this.favoritesMode) {
        DataFavoritesMode.All -> {}
        DataFavoritesMode.OnlyFavorites -> {
            builder.append(" WHERE is_favorite")
            whereUsed = true
        }
        DataFavoritesMode.ExcludeFavorites -> {
            builder.append(" WHERE NOT(is_favorite)")
            whereUsed = true
        }
    }
    when (val range = this.dateRange) {
        DataRecognitionDateRange.Empty -> {}
        is DataRecognitionDateRange.Selected -> {
            builder.append(if (whereUsed) " AND" else " WHERE")
            builder.append(" last_recognition_date BETWEEN ? and ?")
            params.add(range.startDate)
            params.add(range.endDate)
        }
    }
    val sortByColumn = when (this.sortBy) {
        DataSortBy.RecognitionDate -> "last_recognition_date"
        DataSortBy.Title -> "title"
        DataSortBy.Artist -> "artist"
        DataSortBy.ReleaseDate -> "release_date"
    }
    builder.append(" ORDER BY $sortByColumn")
    val orderBy = when (this.orderBy) {
        DataOrderBy.Asc -> "ASC"
        DataOrderBy.Desc -> "DESC"
    }
    builder.append(" $orderBy")
    return SimpleSQLiteQuery(builder.toString(), params.toTypedArray())
}