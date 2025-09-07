package com.mrsep.musicrecognizer.core.data.track

import com.mrsep.musicrecognizer.core.common.di.ApplicationScope
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.core.database.ApplicationDatabase
import com.mrsep.musicrecognizer.core.database.track.TrackEntity
import com.mrsep.musicrecognizer.core.database.track.TrackPreviewTuple
import com.mrsep.musicrecognizer.core.domain.preferences.TrackFilter
import com.mrsep.musicrecognizer.core.domain.track.TrackRepository
import com.mrsep.musicrecognizer.core.domain.track.model.SearchResult
import com.mrsep.musicrecognizer.core.domain.track.model.Track
import com.mrsep.musicrecognizer.core.domain.track.model.TrackDataField
import com.mrsep.musicrecognizer.core.domain.track.model.TrackPreview
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class TrackRepositoryImpl @Inject constructor(
    @ApplicationScope private val appScope: CoroutineScope,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    database: ApplicationDatabase
) : TrackRepository {

    private val trackDao = database.trackDao()
    private val persistentCoroutineContext = appScope.coroutineContext + ioDispatcher

    override suspend fun upsert(tracks: List<Track>) {
        withContext(persistentCoroutineContext) {
            trackDao.upsert(tracks.map(Track::toEntity))
        }
    }

    override suspend fun update(tracks: List<Track>) {
        withContext(persistentCoroutineContext) {
            trackDao.update(tracks.map(Track::toEntity))
        }
    }

    override suspend fun upsertKeepProperties(tracks: List<Track>): List<Track> {
        return withContext(persistentCoroutineContext) {
            trackDao.upsertKeepProperties(tracks.map(Track::toEntity)).map(TrackEntity::toDomain)
        }
    }

    override suspend fun updateKeepProperties(tracks: List<Track>) {
        return withContext(persistentCoroutineContext) {
            trackDao.updateKeepProperties(tracks.map(Track::toEntity))
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

    override fun getTrackFlow(trackId: String): Flow<Track?> {
        return trackDao.getTrackFlow(trackId)
            .map { entity -> entity?.toDomain() }
            .flowOn(ioDispatcher)
    }

    override fun getPreviewsByFilterFlow(
        filter: TrackFilter
    ): Flow<List<TrackPreview>> {
        return trackDao.getPreviewsFlowByFilter(
            favoritesMode = filter.favoritesMode,
            startDate = filter.dateRange.first,
            endDate = filter.dateRange.last,
            sortBy = filter.sortBy,
            orderBy = filter.orderBy
        )
            .map { list -> list.map(TrackPreviewTuple::toDomain) }
            .flowOn(ioDispatcher)
    }

    override fun getSearchResultFlow(
        query: String,
        searchScope: Set<TrackDataField>
    ): Flow<SearchResult> {
        return trackDao.getPreviewsFlowByQuery(query, searchScope)
            .map<List<TrackPreviewTuple>, SearchResult> { list ->
                SearchResult.Success(
                    query = query,
                    searchScope = searchScope,
                    data = list.map(TrackPreviewTuple::toDomain)
                )
            }
            .onStart { emit(SearchResult.Pending(query, searchScope)) }
            .flowOn(ioDispatcher)
    }
}
