package com.mrsep.musicrecognizer.feature.track.presentation.track

import android.net.Uri
import androidx.compose.foundation.background
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
import com.mrsep.musicrecognizer.core.ui.util.openUrlImplicitly
import com.mrsep.musicrecognizer.core.ui.util.shareText
import com.mrsep.musicrecognizer.feature.track.presentation.utils.SwitchingMusicRecognizerTheme

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
    val context = LocalContext.current
    val topBarBehaviour = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val screenUiState by viewModel.uiStateStream.collectAsStateWithLifecycle()

    when (val uiState = screenUiState) {

        TrackUiState.Loading -> Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
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
                artworkBasedThemeEnabled = uiState.artworkBasedThemeEnabled
            ) {
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
                var artworkUri by remember { mutableStateOf<Uri?>(null) }
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
                            isLyricsAvailable = uiState.isLyricsAvailable,
                            onLyricsClick = { onNavigateToLyricsScreen(uiState.mbId) },
                            onShareClick = {
                                context.shareText(
                                    subject = "",
                                    body = uiState.sharedBody
                                )
//                                artworkUri?.let { uri ->
//                                    context.shareImageWithText(
//                                        subject = "",
//                                        body = uiState.sharedBody,
//                                        imageUri = uri
//                                    )
//                                } ?: let {
//                                    context.shareText(
//                                        subject = "",
//                                        body = uiState.sharedBody
//                                    )
//                                }
                            },
                            onDeleteClick = { viewModel.deleteTrack(uiState.mbId) },
                            onShowDetailsClick = { extraDataDialogVisible = true },
                            onOpenOdesliClick = { context.openUrlImplicitly(uiState.odesliLink) },
                            topAppBarScrollBehavior = topBarBehaviour
                        )
                        TrackSection(
                            title = uiState.title,
                            artist = uiState.artist,
                            albumAndYear = uiState.albumAndYear,
                            artworkUrl = uiState.artworkUrl,
                            links = uiState.links,
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
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}