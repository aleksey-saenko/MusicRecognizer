package com.mrsep.musicrecognizer.core.data.track

import com.mrsep.musicrecognizer.core.common.di.ApplicationScope
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.core.database.ApplicationDatabase
import com.mrsep.musicrecognizer.core.database.track.TrackEntity
import com.mrsep.musicrecognizer.core.database.track.TrackPreviewTuple
import com.mrsep.musicrecognizer.core.domain.preferences.FavoritesMode
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
import com.mrsep.musicrecognizer.core.metadata.tracklink.RemoteTrackLinks
import com.mrsep.musicrecognizer.core.metadata.tracklink.odesli.OdesliTrackLinksFetcher
import com.mrsep.musicrecognizer.core.metadata.tracklink.qobuz.QobuzTrackLinksFetcher
import com.mrsep.musicrecognizer.core.metadata.tracklink.youtube.YoutubeTrackLinksFetcher
import com.mrsep.musicrecognizer.core.metadata.lyrics.LyricsFetcher
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
    private val odesliTrackLinksFetcher: OdesliTrackLinksFetcher,
    private val youtubeTrackLinksFetcher: YoutubeTrackLinksFetcher,
    private val qobuzTrackLinksFetcher: QobuzTrackLinksFetcher,
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

    override fun getTracksFlow(favoritesMode: FavoritesMode): Flow<List<Track>> {
        return trackDao.getTracksFlow(favoritesMode)
            .map { list -> list.map(TrackEntity::toDomain) }
            .flowOn(ioDispatcher)
    }

    override suspend fun fetchAndUpdateTrackLinks(
        trackId: String,
        requiredServices: Set<MusicService>
    ): NetworkResult<Unit> = withContext(ioDispatcher) {
        if (requiredServices.isEmpty()) return@withContext NetworkResult.Success(Unit)
        val fetchers = listOf(
            odesliTrackLinksFetcher,
            youtubeTrackLinksFetcher,
//            qobuzTrackLinksFetcher,
        ).filter { fetcher ->
            requiredServices.any(fetcher.supportedServices::contains)
        }
        var lastError: NetworkError? = null
        for (fetcher in fetchers) {
            // Null if track was not found or was deleted in process
            val track = getTrackFlow(trackId).first() ?: return@withContext NetworkResult.Success(Unit)

            val availableServices = fetcher.supportedServices intersect requiredServices
            val shouldSearchForLinks = availableServices.any { it !in track.trackLinks }
            if (!shouldSearchForLinks) return@withContext NetworkResult.Success(Unit)

            when (val result = fetcher.fetch(track)) {
                is NetworkResult.Success -> {
                    trackDao.updateTransform(trackId) { previous ->
                        previous.copy(links = previous.links.merge(result.data))
                    }
                }
                is NetworkError -> {
                    lastError = result
                }
            }
        }
        lastError ?: NetworkResult.Success(Unit)
    }

    override suspend fun fetchAndUpdateLyrics(
        trackId: String
    ): NetworkResult<Unit> = withContext(ioDispatcher) {
        // Null if track was not found or was deleted in process
        val track = getTrackFlow(trackId).first() ?: return@withContext NetworkResult.Success(Unit)

        when (val result = lyricsFetcher.fetch(track)) {
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
    qobuz = qobuz ?: remote.trackLinks[MusicService.Qobuz],
    soundCloud = soundCloud ?: remote.trackLinks[MusicService.Soundcloud],
    spotify = spotify ?: remote.trackLinks[MusicService.Spotify],
    tidal = tidal ?: remote.trackLinks[MusicService.Tidal],
    yandexMusic = yandexMusic ?: remote.trackLinks[MusicService.YandexMusic],
    youtube = youtube ?: remote.trackLinks[MusicService.Youtube],
    youtubeMusic = youtubeMusic ?: remote.trackLinks[MusicService.YoutubeMusic],
)
