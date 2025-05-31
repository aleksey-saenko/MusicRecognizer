package com.mrsep.musicrecognizer.feature.track.presentation.track

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrsep.musicrecognizer.core.domain.preferences.ThemeMode
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
                                    isLoadingLinks = uiState.isMetadataEnhancerRunning,
                                    isExpandedScreen = isExpandedScreen,
                                    onArtworkClick = {
                                        artworkShieldUrl = uiState.track.artworkUrl
                                    },
                                    createSeedColor = uiState.artworkBasedThemeEnabled,
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
                                    isRetryAllowed = isRetryAllowed,
                                    onFavoriteClick = {
                                        viewModel.setFavorite(
                                            uiState.track.id,
                                            !uiState.track.isFavorite
                                        )
                                    },
                                    onLyricsClick = {
                                        onNavigateToLyricsScreen(uiState.track.id)
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

private fun performWebSearch(
    context: Context,
    searchParams: SearchParams,
    track: TrackUi,
) {
    val query = track.getWebSearchQuery(searchParams.target, context)
    when (searchParams.provider) {
        SearchProvider.WebDefault -> context.openWebSearchImplicitly(query)
        SearchProvider.Wikipedia -> context.openWikiSearch(query)
    }
}

private fun TrackUi.getWebSearchQuery(target: SearchTarget, context: Context) = when (target) {
    SearchTarget.Track -> "$title $artist"
    SearchTarget.Artist -> artist
    SearchTarget.Album -> album?.run { "$this $artist" }
        ?: "$title $artist ${context.getString(StringsR.string.album)}"
}

private fun Context.openWikiSearch(query: String) {
    val wikiSendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        `package` = "org.wikipedia"
        putExtra(Intent.EXTRA_TEXT, query)
    }
    try {
        startActivity(wikiSendIntent)
    } catch (e: ActivityNotFoundException) {
        val lang = Locale.getDefault().language
        val encodedQuery = Uri.encode(query)
        val url = "https://$lang.wikipedia.org/wiki/Special:Search?search=$encodedQuery"
        openUrlImplicitly(url)
    }
}
