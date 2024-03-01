package com.mrsep.musicrecognizer.feature.track.presentation.track

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrsep.musicrecognizer.core.ui.components.EmptyStaticTopBar
import com.mrsep.musicrecognizer.core.ui.components.LoadingStub
import com.mrsep.musicrecognizer.core.ui.util.copyTextToClipboard
import com.mrsep.musicrecognizer.core.ui.util.openUrlImplicitly
import com.mrsep.musicrecognizer.core.ui.util.openWebSearchImplicitly
import com.mrsep.musicrecognizer.core.ui.util.shareText
import com.mrsep.musicrecognizer.feature.track.domain.model.ThemeMode
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
    onTrackDeleted: () -> Unit
) {
    val screenUiState by viewModel.uiStateStream.collectAsStateWithLifecycle()

    when (val uiState = screenUiState) {

        TrackUiState.Loading -> LoadingStub(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .systemBarsPadding()
        )

        TrackUiState.TrackNotFound -> Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            EmptyStaticTopBar(onBackPressed = onBackPressed)
            TrackNotFoundMessage(
                modifier = Modifier.weight(weight = 1f)
            )
            Spacer(Modifier.height(64.dp))
        }

        is TrackUiState.Success -> {
            SwitchingMusicRecognizerTheme(
                seedColor = uiState.track.themeSeedColor?.run(::Color),
                artworkBasedThemeEnabled = uiState.artworkBasedThemeEnabled,
                useDarkTheme = shouldUseDarkTheme(uiState.themeMode)
            ) {
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                var artworkUri by remember { mutableStateOf<Uri?>(null) }
                var shareSheetActive by rememberSaveable { mutableStateOf(false) }
                val shareSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                fun hideShareSheet() {
                    scope.launch { shareSheetState.hide() }.invokeOnCompletion {
                        if (!shareSheetState.isVisible) shareSheetActive = false
                    }
                }

                if (shareSheetActive) {
                    ShareBottomSheet(
                        track = uiState.track,
                        sheetState = shareSheetState,
                        onDismissRequest = { hideShareSheet() },
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

                var extraDataDialogVisible by remember { mutableStateOf(false) }
                if (extraDataDialogVisible) {
                    TrackExtrasDialog(
                        track = uiState.track,
                        onDismissClick = { extraDataDialogVisible = false }
                    )
                }
                val trackExistenceState by viewModel.trackExistingState.collectAsStateWithLifecycle()
                var trackDismissed by rememberSaveable { mutableStateOf(false) }
                LaunchedEffect(trackExistenceState) {
                    if (!trackExistenceState && !trackDismissed) onTrackDeleted()
                }

                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxSize()
                ) {
                    val screenScrollState = rememberScrollState()
                    val topBarBehaviour = TopAppBarDefaults.pinnedScrollBehavior()

                    var showSearchBottomSheet by remember { mutableStateOf(false) }
                    if (showSearchBottomSheet) {
                        WebSearchBottomSheet(
                            sheetState = rememberModalBottomSheetState(true),
                            onPerformWebSearchClick = { searchParams ->
                                performWebSearch(context, searchParams, uiState.track)
                            },
                            onDismissRequest = { showSearchBottomSheet = false },
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                    ) {
                        TrackScreenTopBar(
                            onBackPressed = onBackPressed,
                            onShareClick = { shareSheetActive = !shareSheetActive },
                            onDeleteClick = { viewModel.deleteTrack(uiState.track.id) },
                            onShowDetailsClick = { extraDataDialogVisible = true },
                            scrollBehavior = topBarBehaviour
                        )
                        TrackSection(
                            track = uiState.track,
                            isLoadingLinks = uiState.isMetadataEnhancerRunning,
                            isExpandedScreen = isExpandedScreen,
                            onArtworkCached = { artworkUri = it },
                            createSeedColor = uiState.artworkBasedThemeEnabled,
                            onSeedColor = { seedColor ->
                                viewModel.setThemeSeedColor(
                                    uiState.track.id,
                                    seedColor.toArgb()
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
//                                .nestedScroll(bottomBarBehaviour.nestedScrollConnection)
                                .nestedScroll(topBarBehaviour.nestedScrollConnection)
                                .verticalScroll(screenScrollState)

                        )
                        TrackActionsBottomBar(
                            scrollBehavior = null,
                            isFavorite = uiState.track.isFavorite,
                            isLyricsAvailable = uiState.track.lyrics != null,
                            isRetryAllowed = isRetryAllowed,
                            onFavoriteClick = {
                                viewModel.setFavorite(uiState.track.id, !uiState.track.isFavorite)
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
    track: TrackUi
) {
    val query = track.getWebSearchQuery(searchParams.target, context)
    when (searchParams.provider) {
        SearchProvider.WebDefault -> context.openWebSearchImplicitly(query)
        SearchProvider.Wikipedia -> context.openWikiSearch(query)
    }
}

private fun TrackUi.getWebSearchQuery(
    target: SearchTarget, context: Context
) = when (target) {
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