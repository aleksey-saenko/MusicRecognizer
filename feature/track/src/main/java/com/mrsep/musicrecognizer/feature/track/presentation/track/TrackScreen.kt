package com.mrsep.musicrecognizer.feature.track.presentation.track

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrsep.musicrecognizer.core.domain.preferences.ThemeMode
import com.mrsep.musicrecognizer.core.domain.track.model.MusicService
import com.mrsep.musicrecognizer.core.ui.components.LoadingStub
import com.mrsep.musicrecognizer.core.ui.util.copyTextToClipboard
import com.mrsep.musicrecognizer.core.ui.util.openUrlImplicitly
import com.mrsep.musicrecognizer.core.ui.util.openWebSearchImplicitly
import com.mrsep.musicrecognizer.core.ui.util.shareText
import com.mrsep.musicrecognizer.feature.track.presentation.utils.SwitchingMusicRecognizerTheme
import kotlinx.coroutines.launch
import java.util.Locale
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TrackScreen(
    viewModel: TrackViewModel = hiltViewModel(),
    isExpandedScreen: Boolean,
    isRetryAllowed: Boolean,
    onBackPressed: () -> Unit,
    onNavigateToLyricsScreen: (trackId: String) -> Unit,
    onRetryRequested: () -> Unit,
    onTrackDeleted: () -> Unit,
) {
    val screenUiState by viewModel.uiStateStream.collectAsStateWithLifecycle()
    val topBarBehaviour = TopAppBarDefaults.pinnedScrollBehavior()

    when (val uiState = screenUiState) {
        TrackUiState.Loading -> Column(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surface)
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            TrackScreenLoadingTopBar(
                onBackPressed = onBackPressed,
                scrollBehavior = topBarBehaviour
            )
            LoadingStub(modifier = Modifier.fillMaxSize())
        }

        TrackUiState.TrackNotFound -> Column(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surface)
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            TrackScreenLoadingTopBar(
                onBackPressed = onBackPressed,
                scrollBehavior = topBarBehaviour
            )
            TrackNotFoundMessage(modifier = Modifier.fillMaxSize())
        }

        is TrackUiState.Success -> {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            var artworkShieldUrl by rememberSaveable { mutableStateOf<String?>(null) }
            var showTrackExtrasDialog by rememberSaveable { mutableStateOf(false) }
            var showShareSheet by rememberSaveable { mutableStateOf(false) }
            var showSearchBottomSheet by rememberSaveable { mutableStateOf(false) }

            val shareSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            val searchSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            fun hideShareSheet() {
                scope.launch { shareSheetState.hide() }.invokeOnCompletion {
                    if (!shareSheetState.isVisible) showShareSheet = false
                }
            }
            fun hideSearchSheet() {
                scope.launch { searchSheetState.hide() }.invokeOnCompletion {
                    if (!searchSheetState.isVisible) showSearchBottomSheet = false
                }
            }

            val trackExistenceState by viewModel.trackExistingState.collectAsStateWithLifecycle()
            var trackDismissed by rememberSaveable { mutableStateOf(false) }
            LaunchedEffect(trackExistenceState) {
                if (!trackExistenceState && !trackDismissed) onTrackDeleted()
            }

            LaunchedEffect(uiState.isTrackViewed) {
                if (!uiState.isTrackViewed) viewModel.setTrackAsViewed(uiState.track.id)
            }

            SwitchingMusicRecognizerTheme(
                seedColor = uiState.track.themeSeedColor?.run(::Color),
                artworkBasedThemeEnabled = uiState.artworkBasedThemeEnabled,
                useDarkTheme = shouldUseDarkTheme(uiState.themeMode),
                usePureBlackForDarkTheme = uiState.usePureBlackForDarkTheme,
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AnimatedContent(
                        modifier = Modifier.fillMaxSize(),
                        targetState = artworkShieldUrl,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(250)))
                                .togetherWith(fadeOut(animationSpec = tween(250)))
                        },
                        label = "showArtworkShield"
                    ) { shieldUrl ->
                        if (shieldUrl == null) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                val screenScrollState = rememberScrollState()
                                TrackScreenTopBar(
                                    onBackPressed = onBackPressed,
                                    onShareClick = { showShareSheet = !showShareSheet },
                                    onDeleteClick = { viewModel.deleteTrack(uiState.track.id) },
                                    onShowDetailsClick = { showTrackExtrasDialog = true },
                                    scrollBehavior = topBarBehaviour
                                )
                                TrackSection(
                                    track = uiState.track,
                                    isLoadingLinks = uiState.isTrackLinksFetcherRunning,
                                    isExpandedScreen = isExpandedScreen,
                                    onArtworkClick = {
                                        artworkShieldUrl = uiState.track.artworkUrl
                                    },
                                    createSeedColor = uiState.artworkBasedThemeEnabled &&
                                            uiState.track.themeSeedColor == null,
                                    onSeedColorCreated = { seedColor ->
                                        if (seedColor == uiState.track.themeSeedColor) return@TrackSection
                                        viewModel.setThemeSeedColor(uiState.track.id, seedColor)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
//                                        .nestedScroll(bottomBarBehaviour.nestedScrollConnection)
                                        .nestedScroll(topBarBehaviour.nestedScrollConnection)
                                        .verticalScroll(screenScrollState)

                                )
                                TrackActionsBottomBar(
                                    scrollBehavior = null,
                                    isFavorite = uiState.track.isFavorite,
                                    isLyricsAvailable = uiState.track.lyrics != null,
                                    isLyricsLoading = uiState.isLyricsFetcherRunning,
                                    isRetryAllowed = isRetryAllowed,
                                    onFavoriteClick = {
                                        viewModel.setFavorite(uiState.track.id, !uiState.track.isFavorite)
                                    },
                                    onLyricsClick = {
                                        if (uiState.track.lyrics != null) {
                                            onNavigateToLyricsScreen(uiState.track.id)
                                        } else if (uiState.isLyricsFetcherRunning) {
                                            context.toast(StringsR.string.searching_for_lyrics)
                                        } else {
                                            context.toast(StringsR.string.no_lyrics_available)
                                        }
                                    },
                                    onSearchClick = {
                                        showSearchBottomSheet = true
                                    },
                                    onRetryRequested = {
                                        trackDismissed = true
                                        viewModel.deleteTrack(uiState.track.id)
                                        onRetryRequested()
                                    }
                                )
                                if (showShareSheet) {
                                    ShareBottomSheet(
                                        sheetState = shareSheetState,
                                        onDismissRequest = { showShareSheet = false },
                                        track = uiState.track,
                                        onCopyClick = { textToCopy ->
                                            hideShareSheet()
                                            context.copyTextToClipboard(textToCopy)
                                        },
                                        onShareClick = { textToShare ->
                                            hideShareSheet()
                                            context.shareText(
                                                subject = "",
                                                body = textToShare
                                            )
                                        }
                                    )
                                }
                                if (showSearchBottomSheet) {
                                    WebSearchBottomSheet(
                                        sheetState = searchSheetState,
                                        onDismissRequest = { showSearchBottomSheet = false },
                                        requiredServices = uiState.requiredServices,
                                        albumAvailable = uiState.track.album != null,
                                        onPerformWebSearchClick = { searchParams ->
                                            hideSearchSheet()
                                            performWebSearch(context, searchParams, uiState.track)
                                        },
                                    )
                                }
                                if (showTrackExtrasDialog) {
                                    TrackExtrasDialog(
                                        track = uiState.track,
                                        onDismissClick = { showTrackExtrasDialog = false }
                                    )
                                }
                            }
                        } else {
                            AlbumArtworkShield(
                                artworkUrl = shieldUrl,
                                title = uiState.track.title,
                                artist = uiState.track.artist,
                                album = uiState.track.album,
                                year = uiState.track.year,
                                onBackPressed = { artworkShieldUrl = null },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun shouldUseDarkTheme(
    themeMode: ThemeMode,
): Boolean = when (themeMode) {
    ThemeMode.FollowSystem -> isSystemInDarkTheme()
    ThemeMode.AlwaysLight -> false
    ThemeMode.AlwaysDark -> true
}

// TODO refactor search module

private fun performWebSearch(
    context: Context,
    searchParams: SearchParams,
    track: TrackUi,
) {
    when (searchParams.provider) {
        SearchProvider.WebDefault -> {
            val query = track.genericSearchQuery(searchParams.target)
            context.openWebSearchImplicitly(query)
        }
        SearchProvider.Wikipedia -> {
            val query = track.genericSearchQuery(searchParams.target)
            context.openWikiSearch(query)
        }
        is SearchProvider.Service -> {
            val url = searchParams.provider.service.createSearchUrlFor(track, searchParams.target)
            context.openUrlImplicitly(url)
        }
    }
}

private fun TrackUi.genericSearchQuery(target: SearchTarget): String = when (target) {
    SearchTarget.Track -> "$title $artist".trim()
    SearchTarget.Artist -> artist.trim()
    SearchTarget.Album -> album?.trim()?.takeIf { it.isNotBlank() }?.run { "$this $artist" }?.trim()
        ?: "$title $artist".trim()
}

private fun Context.openWikiSearch(query: String) {
    val wikiSendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        `package` = "org.wikipedia"
        putExtra(Intent.EXTRA_TEXT, query)
    }
    try {
        startActivity(wikiSendIntent)
    } catch (_: ActivityNotFoundException) {
        val lang = Locale.getDefault().language
        val encodedQuery = Uri.encode(query)
        val url = "https://$lang.wikipedia.org/wiki/Special:Search?search=$encodedQuery"
        openUrlImplicitly(url)
    }
}

private fun MusicService.createSearchUrlFor(track: TrackUi, target: SearchTarget): String {
    val queryEncoded = track.genericSearchQuery(target).urlEncode()
    return when (this) {
        MusicService.AmazonMusic -> when (target) {
            SearchTarget.Track ->   "https://music.amazon.com/search/${queryEncoded}"
            SearchTarget.Artist ->  "https://music.amazon.com/search/${queryEncoded}/artists"
            SearchTarget.Album ->   "https://music.amazon.com/search/${queryEncoded}/albums"
        }
        MusicService.Anghami -> when (target) {
            SearchTarget.Track ->   "https://play.anghami.com/search/${queryEncoded}/song"
            SearchTarget.Artist ->  "https://play.anghami.com/search/${queryEncoded}/artist"
            SearchTarget.Album ->   "https://play.anghami.com/search/${queryEncoded}/album"
        }
        MusicService.AppleMusic ->  "https://music.apple.com/us/search?term=${queryEncoded}"
        MusicService.Audiomack -> when (target) {
            SearchTarget.Track ->   "https://audiomack.com/search?q=${queryEncoded}&show=songs"
            SearchTarget.Artist ->  "https://audiomack.com/search?q=${queryEncoded}&show=artists"
            SearchTarget.Album ->   "https://audiomack.com/search?q=${queryEncoded}&show=albums"
        }
        MusicService.Audius ->      "https://audius.co/search?query=${queryEncoded}"
        MusicService.Boomplay -> when (target) {
            SearchTarget.Track ->   "https://www.boomplay.com/search/default/${queryEncoded}"
            SearchTarget.Artist ->  "https://www.boomplay.com/search/artist/${queryEncoded}"
            SearchTarget.Album ->   "https://www.boomplay.com/search/album/${queryEncoded}"
        }
        MusicService.Deezer -> when (target) {
            SearchTarget.Track ->   "https://www.deezer.com/search/${queryEncoded}/track"
            SearchTarget.Artist ->  "https://www.deezer.com/search/${queryEncoded}/artist"
            SearchTarget.Album ->   "https://www.deezer.com/search/${queryEncoded}/album"
        }
        MusicService.MusicBrainz -> when (target) {
            SearchTarget.Track ->   "https://musicbrainz.org/search?query=${queryEncoded}&type=recording&limit=25&method=indexed"
            SearchTarget.Artist ->  "https://musicbrainz.org/search?query=${queryEncoded}&type=artist&limit=25&method=indexed"
            SearchTarget.Album ->   "https://musicbrainz.org/search?query=${queryEncoded}&type=release&limit=25&method=indexed"
        }
        // TODO is this service still relevant? consider delete
        MusicService.Napster ->     "https://app.napster.com/search?q=${queryEncoded}"
        MusicService.Pandora -> when (target) {
            SearchTarget.Track ->   "https://www.pandora.com/search/${queryEncoded}/songs"
            SearchTarget.Artist ->  "https://www.pandora.com/search/${queryEncoded}/artists"
            SearchTarget.Album ->   "https://www.pandora.com/search/${queryEncoded}/albums"
        }
        MusicService.Qobuz -> when (target) {
            SearchTarget.Track ->   "https://www.qobuz.com/gb-en/search/tracks/${queryEncoded}"
            SearchTarget.Artist ->  "https://www.qobuz.com/gb-en/search/artists/${queryEncoded}"
            SearchTarget.Album ->   "https://www.qobuz.com/gb-en/search/albums/${queryEncoded}"
        }
        MusicService.Soundcloud -> when (target) {
            SearchTarget.Track ->   "https://soundcloud.com/search/sounds?q=${queryEncoded}"
            SearchTarget.Artist ->  "https://soundcloud.com/search/people?q=${queryEncoded}"
            SearchTarget.Album ->   "https://soundcloud.com/search/albums?q=${queryEncoded}"
        }
        MusicService.Spotify -> when (target) {
            SearchTarget.Track ->   "https://open.spotify.com/search/${queryEncoded}/tracks"
            SearchTarget.Artist ->  "https://open.spotify.com/search/${queryEncoded}/artists"
            SearchTarget.Album ->   "https://open.spotify.com/search/${queryEncoded}/albums"
        }
        MusicService.Tidal -> when (target) {
            SearchTarget.Track ->   "https://tidal.com/search/tracks?q=${queryEncoded}"
            SearchTarget.Artist ->  "https://tidal.com/search/artists?q=${queryEncoded}"
            SearchTarget.Album ->   "https://tidal.com/search/albums?q=${queryEncoded}"
        }
        MusicService.YandexMusic -> "https://music.yandex.ru/search?text=${queryEncoded}"
        MusicService.Youtube ->     "https://www.youtube.com/results?search_query=${queryEncoded}"
        MusicService.YoutubeMusic -> "https://music.youtube.com/search?q=${queryEncoded}"
    }
}

private fun String.urlEncode(): String = Uri.encode(trim())

private fun Context.toast(@StringRes stringRes: Int) {
    Toast.makeText(this, getString(stringRes), Toast.LENGTH_SHORT).show()
}
