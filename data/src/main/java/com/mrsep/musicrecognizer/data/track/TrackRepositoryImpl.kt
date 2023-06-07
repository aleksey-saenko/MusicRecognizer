package com.mrsep.musicrecognizer.data.track

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.mrsep.musicrecognizer.core.common.di.ApplicationScope
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.data.database.ApplicationDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TrackRepositoryImpl @Inject constructor(
    @ApplicationScope private val appScope: CoroutineScope,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    database: ApplicationDatabase
) : TrackRepositoryDo {
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
        withContext(appScope.coroutineContext + ioDispatcher) {
            trackDao.delete(*track)
        }
    }

    override suspend fun deleteAll() {
        withContext(appScope.coroutineContext + ioDispatcher) {
            trackDao.deleteAll()
        }
    }

    override suspend fun deleteAllExceptFavorites() {
        withContext(appScope.coroutineContext + ioDispatcher) {
            trackDao.deleteAllExceptFavorites()
        }
    }

    override suspend fun deleteAllFavorites() {
        withContext(appScope.coroutineContext + ioDispatcher) {
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

    override fun getFilteredFlow(filter: TrackFilterDo): Flow<List<TrackEntity>> {
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

    override fun searchResultFlow(keyword: String, limit: Int): Flow<SearchResultDo<TrackEntity>> {
        val searchKey = createSearchKeyForSQLite(keyword)
        return trackDao.searchFlow(searchKey, ESCAPE_SYMBOL, limit)
            .map<List<TrackEntity>, SearchResultDo<TrackEntity>> { list ->
//                delay(3000) //debug purpose
                SearchResultDo.Success(
                    keyword = keyword,
                    data = list
                )
            }
            .onStart { emit(SearchResultDo.Pending(keyword)) }
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

private fun TrackFilterDo.toSQLiteQuery(): SupportSQLiteQuery {
    val builder = StringBuilder("SELECT * FROM track")
    val params = mutableListOf<Any>()
    var whereUsed = false
    when (this.favoritesMode) {
        DataFavoritesModeDo.All -> {}
        DataFavoritesModeDo.OnlyFavorites -> {
            builder.append(" WHERE is_favorite")
            whereUsed = true
        }
        DataFavoritesModeDo.ExcludeFavorites -> {
            builder.append(" WHERE NOT(is_favorite)")
            whereUsed = true
        }
    }
    when (val range = this.dateRange) {
        DataRecognitionDateRangeDo.Empty -> {}
        is DataRecognitionDateRangeDo.Selected -> {
            builder.append(if (whereUsed) " AND" else " WHERE")
            builder.append(" last_recognition_date BETWEEN ? and ?")
            params.add(range.startDate)
            params.add(range.endDate)
        }
    }
    val sortByColumn = when (this.sortBy) {
        DataSortByDo.RecognitionDate -> "last_recognition_date"
        DataSortByDo.Title -> "title"
        DataSortByDo.Artist -> "artist"
        DataSortByDo.ReleaseDate -> "release_date"
    }
    builder.append(" ORDER BY $sortByColumn")
    val orderBy = when (this.orderBy) {
        DataOrderByDo.Asc -> "ASC"
        DataOrderByDo.Desc -> "DESC"
    }
    builder.append(" $orderBy")
    return SimpleSQLiteQuery(builder.toString(), params.toTypedArray())
}