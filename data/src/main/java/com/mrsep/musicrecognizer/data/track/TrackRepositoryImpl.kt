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

internal class TrackRepositoryImpl @Inject constructor(
    @ApplicationScope private val appScope: CoroutineScope,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    database: ApplicationDatabase
) : TrackRepositoryDo {

    private val trackDao = database.trackDao()
    private val persistentCoroutineContext = appScope.coroutineContext + ioDispatcher

    override suspend fun upsert(vararg tracks: TrackEntity) {
        withContext(persistentCoroutineContext) {
            trackDao.upsert(*tracks)
        }
    }

    override suspend fun update(vararg tracks: TrackEntity) {
        withContext(persistentCoroutineContext) {
            trackDao.update(*tracks)
        }
    }

    override suspend fun upsertKeepUserProperties(vararg tracks: TrackEntity): List<TrackEntity> {
        return withContext(persistentCoroutineContext) {
            trackDao.upsertKeepUserProperties(*tracks)
        }
    }

    override suspend fun updateKeepUserProperties(vararg tracks: TrackEntity) {
        return withContext(persistentCoroutineContext) {
            trackDao.updateKeepUserProperties(*tracks)
        }
    }

    override suspend fun setFavorite(trackId: String, isFavorite: Boolean) {
        withContext(persistentCoroutineContext) {
            trackDao.setFavorite(trackId, isFavorite)
        }
    }

    override suspend fun setAsViewed(trackId: String) {
        withContext(persistentCoroutineContext) {
            trackDao.setAsViewed(trackId)
        }
    }

    override suspend fun setThemeSeedColor(trackId: String, color: Int?) {
        withContext(persistentCoroutineContext) {
            trackDao.setThemeSeedColor(trackId, color)
        }
    }

    override suspend fun delete(vararg trackIds: String) {
        withContext(persistentCoroutineContext) {
            trackDao.delete(*trackIds)
        }
    }

    override suspend fun deleteAll() {
        withContext(persistentCoroutineContext) {
            trackDao.deleteAll()
        }
    }

    override fun isEmptyFlow(): Flow<Boolean> {
        return trackDao.isEmptyDatabaseFlow()
            .distinctUntilChanged()
            .flowOn(ioDispatcher)
    }

    override fun getUnviewedCountFlow(): Flow<Int> {
        return trackDao.getUnviewedCountFlow()
            .distinctUntilChanged()
            .flowOn(ioDispatcher)
    }

    override suspend fun getTrack(trackId: String): TrackEntity? {
        return withContext(ioDispatcher) {
            trackDao.getTrack(trackId)
        }
    }

    override fun getTrackFlow(trackId: String): Flow<TrackEntity?> {
        return trackDao.getTrackFlow(trackId)
            .flowOn(ioDispatcher)
    }

    override fun getTracksByFilterFlow(
        filter: UserPreferencesDo.TrackFilterDo
    ): Flow<List<TrackEntity>> {
        return trackDao.getTracksFlowByQuery(filter.toSQLiteQuery())
            .flowOn(ioDispatcher)
    }

    override fun getSearchResultFlow(keyword: String, limit: Int): Flow<SearchResultDo> {
        val searchKey = createSearchKeyForSQLite(keyword)
        return trackDao.getTracksFlowByKeyword(searchKey, ESCAPE_SYMBOL, limit)
            .map<List<TrackEntity>, SearchResultDo> { list ->
                SearchResultDo.Success(
                    keyword = keyword,
                    data = list
                )
            }
            .onStart { emit(SearchResultDo.Pending(keyword)) }
            .flowOn(ioDispatcher)
    }

    private fun createSearchKeyForSQLite(word: String): String {
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
        builder.append(" recognition_date BETWEEN ? and ?")
        params.add(this.dateRange.first)
        params.add(this.dateRange.last)
    }
    val sortByColumn = when (this.sortBy) {
        SortByDo.RecognitionDate -> "recognition_date"
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