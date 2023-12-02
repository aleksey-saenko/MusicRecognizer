package com.mrsep.musicrecognizer.feature.track.presentation.track

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrsep.musicrecognizer.core.ui.components.EmptyStaticTopBar
import com.mrsep.musicrecognizer.core.ui.components.LoadingStub
import com.mrsep.musicrecognizer.core.ui.util.copyTextToClipboard
import com.mrsep.musicrecognizer.core.ui.util.shareText
import com.mrsep.musicrecognizer.feature.track.domain.model.ThemeMode
import com.mrsep.musicrecognizer.feature.track.presentation.utils.SwitchingMusicRecognizerTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TrackScreen(
    viewModel: TrackViewModel = hiltViewModel(),
    isExpandedScreen: Boolean,
    isRetryAvailable: Boolean,
    onBackPressed: () -> Unit,
    onNavigateToLyricsScreen: (trackMbId: String) -> Unit,
    onRetryRequested: () -> Unit,
    onTrackDeleted: () -> Unit
) {
    val topBarBehaviour = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val screenUiState by viewModel.uiStateStream.collectAsStateWithLifecycle()

    when (val uiState = screenUiState) {

        TrackUiState.Loading -> Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
                .systemBarsPadding()
        ) {
            EmptyStaticTopBar(onBackPressed = onBackPressed)
            LoadingStub(
                modifier = Modifier.weight(weight = 1f)
            )
            Spacer(Modifier.height(64.dp))
        }

        TrackUiState.TrackNotFound -> Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
                .systemBarsPadding()
        ) {
            EmptyStaticTopBar(onBackPressed = onBackPressed)
            TrackNotFoundMessage(
                modifier = Modifier.weight(weight = 1f)
            )
            Spacer(Modifier.height(64.dp))
        }

        is TrackUiState.Success -> {
            SwitchingMusicRecognizerTheme(
                seedColor = uiState.themeSeedColor?.run(::Color),
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
                        title = uiState.title,
                        artist = uiState.artist,
                        album = uiState.album,
                        year = uiState.year,
                        lyrics = uiState.lyrics,
                        trackLinks = uiState.requiredLinks,
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
                        lastRecognitionDate = uiState.lastRecognitionDate,
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.systemBarsPadding()
                    ) {
                        TrackScreenTopBar(
                            onBackPressed = onBackPressed,
                            isFavorite = uiState.isFavorite,
                            onFavoriteClick = { viewModel.toggleFavoriteMark(uiState.mbId) },
                            isLyricsAvailable = uiState.lyrics != null,
                            onLyricsClick = { onNavigateToLyricsScreen(uiState.mbId) },
                            onShareClick = { shareSheetActive = !shareSheetActive },
                            onDeleteClick = { viewModel.deleteTrack(uiState.mbId) },
                            onShowDetailsClick = { extraDataDialogVisible = true },
                            topAppBarScrollBehavior = topBarBehaviour
                        )
                        TrackSection(
                            title = uiState.title,
                            artist = uiState.artist,
                            albumAndYear = uiState.run { year?.let { "$album - $year" } ?: album },
                            artworkUrl = uiState.artworkUrl,
                            trackLinks = uiState.requiredLinks,
                            isExpandedScreen = isExpandedScreen,
                            onArtworkCached = { artworkUri = it },
                            createSeedColor = uiState.artworkBasedThemeEnabled,
                            onSeedColor = { seedColor ->
                                viewModel.updateThemeSeedColor(
                                    uiState.mbId,
                                    seedColor.toArgb()
                                )
                            },
                            isRetryAvailable = isRetryAvailable,
                            onRetryRequested = {
                                trackDismissed = true
                                viewModel.deleteTrack(uiState.mbId)
                                onRetryRequested()
                            },
                            onCopyToClipboard = { context.copyTextToClipboard(it) },
                            modifier = Modifier.fillMaxSize()
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