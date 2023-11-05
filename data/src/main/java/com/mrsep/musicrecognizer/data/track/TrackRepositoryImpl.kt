package com.mrsep.musicrecognizer.data.track

import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.mrsep.musicrecognizer.core.common.di.ApplicationScope
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.data.database.ApplicationDatabase
import com.mrsep.musicrecognizer.data.preferences.FavoritesModeDo
import com.mrsep.musicrecognizer.data.preferences.OrderByDo
import com.mrsep.musicrecognizer.data.preferences.SortByDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
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
    private val persistentCoroutineContext = appScope.coroutineContext + ioDispatcher

    override fun isEmptyFlow(): Flow<Boolean> {
        return trackDao.isEmptyFlow()
            .distinctUntilChanged()
            .flowOn(ioDispatcher)
    }

    override suspend fun insertOrReplace(vararg track: TrackEntity) {
        withContext(persistentCoroutineContext) {
            trackDao.insertOrReplace(*track)
        }
    }

    override suspend fun insertOrReplaceSaveMetadata(vararg track: TrackEntity): List<TrackEntity> {
        return withContext(persistentCoroutineContext) {
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

    override suspend fun updateThemeSeedColor(mbId: String, color: Int?) {
        withContext(persistentCoroutineContext) {
            trackDao.updateThemeSeedColor(mbId, color)
        }
    }

    override suspend fun update(track: TrackEntity) {
        withContext(persistentCoroutineContext) {
            trackDao.update(track)
        }
    }

    override suspend fun toggleFavoriteMark(mbId: String) {
        withContext(persistentCoroutineContext) {
            trackDao.toggleFavoriteMark(mbId)
        }
    }

    override suspend fun delete(vararg track: TrackEntity) {
        withContext(persistentCoroutineContext) {
            trackDao.delete(*track)
        }
    }

    override suspend fun deleteByMbId(vararg mbId: String) {
        withContext(persistentCoroutineContext) {
            trackDao.deleteByMbId(*mbId)
        }
    }

    override suspend fun deleteAll() {
        withContext(persistentCoroutineContext) {
            trackDao.deleteAll()
        }
    }

    override suspend fun deleteAllExceptFavorites() {
        withContext(persistentCoroutineContext) {
            trackDao.deleteAllExceptFavorites()
        }
    }

    override suspend fun deleteAllFavorites() {
        withContext(persistentCoroutineContext) {
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

    override fun getFilteredFlow(filter: UserPreferencesDo.TrackFilterDo): Flow<List<TrackEntity>> {
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

    override fun searchResultFlow(keyword: String, limit: Int): Flow<SearchResultDo> {
        val searchKey = createSearchKeyForSQLite(keyword)
        return trackDao.searchFlow(searchKey, ESCAPE_SYMBOL, limit)
            .map<List<TrackEntity>, SearchResultDo> { list ->
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
    }

}

private fun UserPreferencesDo.TrackFilterDo.toSQLiteQuery(): SupportSQLiteQuery {
    val builder = StringBuilder("SELECT * FROM track")
    val params = mutableListOf<Any>()
    var whereUsed = false
    when (this.favoritesMode) {
        FavoritesModeDo.All -> {}
        FavoritesModeDo.OnlyFavorites -> {
            builder.append(" WHERE is_favorite")
            whereUsed = true
        }
        FavoritesModeDo.ExcludeFavorites -> {
            builder.append(" WHERE NOT(is_favorite)")
            whereUsed = true
        }
    }
    val hasUserDateLimits = this.dateRange.run {
        first != Long.MIN_VALUE || last != Long.MAX_VALUE
    }
    if (hasUserDateLimits) {
        builder.append(if (whereUsed) " AND" else " WHERE")
        builder.append(" last_recognition_date BETWEEN ? and ?")
        params.add(this.dateRange.first)
        params.add(this.dateRange.last)
    }
    val sortByColumn = when (this.sortBy) {
        SortByDo.RecognitionDate -> "last_recognition_date"
        SortByDo.Title -> "title"
        SortByDo.Artist -> "artist"
        SortByDo.ReleaseDate -> "release_date"
    }
    builder.append(" ORDER BY $sortByColumn")
    val orderBy = when (this.orderBy) {
        OrderByDo.Asc -> "ASC"
        OrderByDo.Desc -> "DESC"
    }
    builder.append(" $orderBy")
    return SimpleSQLiteQuery(builder.toString(), params.toTypedArray())
}