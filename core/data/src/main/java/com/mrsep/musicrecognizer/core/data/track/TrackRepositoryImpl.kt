package com.mrsep.musicrecognizer.core.data.track

import com.mrsep.musicrecognizer.core.common.di.ApplicationScope
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.core.database.ApplicationDatabase
import com.mrsep.musicrecognizer.core.database.track.TrackEntity
import com.mrsep.musicrecognizer.core.database.track.TrackPreviewTuple
import com.mrsep.musicrecognizer.core.domain.preferences.TrackFilter
import com.mrsep.musicrecognizer.core.domain.recognition.model.NetworkError
import com.mrsep.musicrecognizer.core.domain.recognition.model.NetworkResult
import com.mrsep.musicrecognizer.core.domain.track.TrackRepository
import com.mrsep.musicrecognizer.core.domain.track.model.MusicService
import com.mrsep.musicrecognizer.core.domain.track.model.SearchResult
import com.mrsep.musicrecognizer.core.domain.track.model.SyncedLyrics
import com.mrsep.musicrecognizer.core.domain.track.model.Track
import com.mrsep.musicrecognizer.core.domain.track.model.TrackDataField
import com.mrsep.musicrecognizer.core.domain.track.model.TrackPreview
import com.mrsep.musicrecognizer.core.recognition.enhancer.RemoteTrackLinks
import com.mrsep.musicrecognizer.core.recognition.enhancer.TrackLinksFetcher
import com.mrsep.musicrecognizer.core.recognition.lyrics.LyricsFetcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.collections.map

@OptIn(ExperimentalCoroutinesApi::class)
internal class TrackRepositoryImpl @Inject constructor(
    @ApplicationScope private val appScope: CoroutineScope,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val lyricsFetcher: LyricsFetcher,
    private val trackLinksFetcher: TrackLinksFetcher,
    database: ApplicationDatabase
) : TrackRepository {

    private val trackDao = database.trackDao()
    private val persistentCoroutineContext = appScope.coroutineContext + ioDispatcher

    override suspend fun upsertKeepProperties(tracks: List<Track>): List<Track> {
        return withContext(persistentCoroutineContext) {
            trackDao.upsertKeepProperties(tracks.map(Track::toEntity)).map(TrackEntity::toDomain)
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

    override suspend fun fetchAndUpdateTrackLinks(trackId: String): NetworkResult<Unit> {
        return getTrackFlow(trackId)
            .distinctUntilChangedBy { track -> track?.id }
            .mapLatest { oldTrack ->
                // Null means that track was not found or was deleted in process
                if (oldTrack == null) return@mapLatest NetworkResult.Success(Unit)
                when (val result = trackLinksFetcher.fetch(oldTrack)) {
                    is NetworkResult.Success -> {
                        trackDao.updateTransform(trackId) { previous ->
                            previous.copy(links = previous.links.merge(result.data))
                        }
                        NetworkResult.Success(Unit)
                    }
                    is NetworkError -> result
                }
            }
            .flowOn(ioDispatcher)
            .first()
    }

    override suspend fun fetchAndUpdateLyrics(trackId: String): NetworkResult<Unit> {
        return getTrackFlow(trackId)
            .distinctUntilChangedBy { track -> track?.id }
            .mapLatest { oldTrack ->
                // Null means that track was not found or was deleted in process
                if (oldTrack == null) return@mapLatest NetworkResult.Success(Unit)
                when (val result = lyricsFetcher.fetch(oldTrack)) {
                    is NetworkResult.Success -> {
                        result.data?.let { lyrics ->
                            trackDao.setLyrics(
                                trackId,
                                lyrics = lyrics.toDbLyricsData(),
                                isSynced = lyrics is SyncedLyrics
                            )
                        }
                        NetworkResult.Success(Unit)
                    }
                    is NetworkError -> result
                }
            }
            .flowOn(ioDispatcher)
            .first()
    }
}

private fun TrackEntity.Links.merge(remote: RemoteTrackLinks) = copy(
    artworkThumbnail = artworkThumbnail ?: remote.artworkThumbUrl,
    artwork = artwork ?: remote.artworkUrl,
    amazonMusic = amazonMusic ?: remote.trackLinks[MusicService.AmazonMusic],
    anghami = anghami ?: remote.trackLinks[MusicService.Anghami],
    appleMusic = appleMusic ?: remote.trackLinks[MusicService.AppleMusic],
    audiomack = audiomack ?: remote.trackLinks[MusicService.Audiomack],
    audius = audius ?: remote.trackLinks[MusicService.Audius],
    boomplay = boomplay ?: remote.trackLinks[MusicService.Boomplay],
    deezer = deezer ?: remote.trackLinks[MusicService.Deezer],
    musicBrainz = musicBrainz ?: remote.trackLinks[MusicService.MusicBrainz],
    napster = napster ?: remote.trackLinks[MusicService.Napster],
    pandora = pandora ?: remote.trackLinks[MusicService.Pandora],
    soundCloud = soundCloud ?: remote.trackLinks[MusicService.Soundcloud],
    spotify = spotify ?: remote.trackLinks[MusicService.Spotify],
    tidal = tidal ?: remote.trackLinks[MusicService.Tidal],
    yandexMusic = yandexMusic ?: remote.trackLinks[MusicService.YandexMusic],
    youtube = youtube ?: remote.trackLinks[MusicService.Youtube],
    youtubeMusic = youtubeMusic ?: remote.trackLinks[MusicService.YoutubeMusic],
)
