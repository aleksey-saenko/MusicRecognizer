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

    override suspend fun upsert(tracks: List<TrackEntity>) {
        withContext(persistentCoroutineContext) {
            trackDao.upsert(tracks)
        }
    }

    override suspend fun update(tracks: List<TrackEntity>) {
        withContext(persistentCoroutineContext) {
            trackDao.update(tracks)
        }
    }

    override suspend fun upsertKeepProperties(tracks: List<TrackEntity>): List<TrackEntity> {
        return withContext(persistentCoroutineContext) {
            trackDao.upsertKeepProperties(tracks)
        }
    }

    override suspend fun updateKeepProperties(tracks: List<TrackEntity>) {
        return withContext(persistentCoroutineContext) {
            trackDao.updateKeepProperties(tracks)
        }
    }

    override suspend fun setFavorite(trackId: String, isFavorite: Boolean) {
        withContext(persistentCoroutineContext) {
            trackDao.setFavorite(trackId, isFavorite)
        }
    }

    override suspend fun setViewed(trackId: String, isViewed: Boolean) {
        withContext(persistentCoroutineContext) {
            trackDao.setViewed(trackId, isViewed)
        }
    }

    override suspend fun setThemeSeedColor(trackId: String, color: Int?) {
        withContext(persistentCoroutineContext) {
            trackDao.setThemeSeedColor(trackId, color)
        }
    }

    override suspend fun delete(trackIds: List<String>) {
        withContext(persistentCoroutineContext) {
            trackDao.delete(trackIds)
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

    override fun getTrackFlow(trackId: String): Flow<TrackEntity?> {
        return trackDao.getTrackFlow(trackId)
            .flowOn(ioDispatcher)
    }

    override fun getPreviewsByFilterFlow(
        filter: UserPreferencesDo.TrackFilterDo
    ): Flow<List<TrackPreview>> {
        return trackDao.getPreviewsFlowByQuery(filter.toSQLiteQueryForPreviews())
            .flowOn(ioDispatcher)
    }

    override fun getSearchResultFlow(
        query: String,
        searchScope: Set<TrackDataFieldDo>
    ): Flow<SearchResultDo> {
        val searchPattern = createSearchPatternForSQLite(query)
        return trackDao.getPreviewsFlowByPattern(searchPattern, ESCAPE_SYMBOL, searchScope)
            .map<List<TrackPreview>, SearchResultDo> { list ->
                SearchResultDo.Success(
                    query = query,
                    searchScope = searchScope,
                    data = list
                )
            }
            .onStart { emit(SearchResultDo.Pending(query, searchScope)) }
            .flowOn(ioDispatcher)
    }

    private fun createSearchPatternForSQLite(query: String): String {
        return query
            .replace(ESCAPE_SYMBOL, "${ESCAPE_SYMBOL}/")
            .replace("%", "${ESCAPE_SYMBOL}%")
            .replace("_", "${ESCAPE_SYMBOL}_")
            .run { "%$this%" }
    }

    companion object {
        private const val ESCAPE_SYMBOL = "/"
    }
}

private fun UserPreferencesDo.TrackFilterDo.toSQLiteQueryForPreviews(): SupportSQLiteQuery {
    val builder = StringBuilder("SELECT id, title, artist, album, recognition_date, link_artwork_thumb, link_artwork, is_viewed FROM track")
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
