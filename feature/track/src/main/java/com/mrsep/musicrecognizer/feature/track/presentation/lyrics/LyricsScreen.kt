package com.mrsep.musicrecognizer.feature.track.presentation.lyrics

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrsep.musicrecognizer.core.ui.components.EmptyStaticTopBar
import com.mrsep.musicrecognizer.core.ui.components.LoadingStub
import com.mrsep.musicrecognizer.core.ui.util.shareText
import com.mrsep.musicrecognizer.feature.track.domain.model.FontSize
import com.mrsep.musicrecognizer.feature.track.domain.model.ThemeMode
import com.mrsep.musicrecognizer.feature.track.domain.model.UserPreferences
import com.mrsep.musicrecognizer.feature.track.presentation.track.TrackNotFoundMessage
import com.mrsep.musicrecognizer.feature.track.presentation.track.shouldUseDarkTheme
import com.mrsep.musicrecognizer.feature.track.presentation.utils.SwitchingMusicRecognizerTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun LyricsScreen(
    onBackPressed: () -> Unit,
    viewModel: LyricsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val topBarBehaviour = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val uiStateInFlow by viewModel.uiStateStream.collectAsStateWithLifecycle()

    // Clear focus to hide potential text selection popup
    BackHandler {
        focusManager.clearFocus()
        onBackPressed()
    }

    when (val uiState = uiStateInFlow) {

        LyricsUiState.Loading -> LoadingStub(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surface)
                .fillMaxSize()
                .systemBarsPadding()
        )

        LyricsUiState.LyricsNotFound -> Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surface)
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            EmptyStaticTopBar(onBackPressed = onBackPressed)
            TrackNotFoundMessage(
                modifier = Modifier.fillMaxSize()
            )
        }

        is LyricsUiState.Success -> {
            LaunchedEffect(uiState.isTrackViewed) {
                if (!uiState.isTrackViewed) viewModel.setTrackAsViewed(uiState.trackId)
            }
            val useDarkTheme = shouldUseDarkTheme(uiState.themeMode)
            var showFontStyleBottomSheet by rememberSaveable { mutableStateOf(false) }
            val fontStyleBottomSheetState = rememberModalBottomSheetState(true)
            val lyricsScrollState = rememberScrollState()
            val autoScrollAvailable by remember {
                derivedStateOf {
                    with(lyricsScrollState) { canScrollForward || canScrollBackward }
                }
            }
            fun hideFontStyleBottomSheet() {
                scope.launch { fontStyleBottomSheetState.hide() }.invokeOnCompletion {
                    if (!fontStyleBottomSheetState.isVisible) showFontStyleBottomSheet = false
                }
            }
            SwitchingMusicRecognizerTheme(
                seedColor = uiState.themeSeedColor?.run { Color(this) },
                artworkBasedThemeEnabled = uiState.artworkBasedThemeEnabled,
                useDarkTheme = useDarkTheme,
                highContrastMode = uiState.fontStyle.isHighContrast
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxSize()
                ) {
                    var autoScrollStarted by rememberSaveable { mutableStateOf(false) }
                    var autoScrollPanelVisible by remember { mutableStateOf(false) }
                    var hideScrollPanelJob by remember { mutableStateOf<Job?>(null) }
                    fun requestAutoScrollPanel() {
                        autoScrollPanelVisible = true
                        hideScrollPanelJob?.cancel()
                        hideScrollPanelJob = scope.launch {
                            delay(3_500)
                            autoScrollPanelVisible = false
                        }
                    }
                    LaunchedEffect(autoScrollStarted) {
                        if (autoScrollStarted) {
                            requestAutoScrollPanel()
                        } else {
                            autoScrollPanelVisible = false
                        }
                    }
                    val isLyricsDragged by lyricsScrollState.interactionSource.collectIsDraggedAsState()
                    LaunchedEffect(autoScrollStarted, isLyricsDragged) {
                        if (autoScrollStarted && isLyricsDragged) {
                            requestAutoScrollPanel()
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding()
                    ) {
                        Column {
                            LyricsScreenTopBar(
                                autoScrollAvailable = autoScrollAvailable,
                                autoScrollStarted = autoScrollStarted,
                                onBackPressed = onBackPressed,
                                onShareClick = {
                                    val trackInfo = "${uiState.title} - ${uiState.artist}"
                                    context.shareText(
                                        subject = trackInfo,
                                        body = "$trackInfo\n\n${uiState.lyrics}"
                                    )
                                },
                                onChangeTextStyleClick = { showFontStyleBottomSheet = true },
                                onLaunchAutoScrollClick = { autoScrollStarted = true },
                                onStopAutoScrollClick = { autoScrollStarted = false },
                                scrollBehavior = topBarBehaviour
                            )
                            val currentFontSize = uiState.fontStyle.fontSize
                            var lyricsZoom by remember(currentFontSize) {
                                mutableFloatStateOf(currentFontSize.toZoomValue())
                            }
                            val selectedFontSize by remember(currentFontSize) {
                                derivedStateOf { getFontSize(lyricsZoom) }
                            }
                            val zoomRange = remember {
                                FontSize.Small.toZoomValue()..FontSize.Huge.toZoomValue()
                            }
                            val transformableState =
                                rememberTransformableState { zoomChange, _, _ ->
                                    lyricsZoom = (lyricsZoom * zoomChange).coerceIn(zoomRange)
                                }
                            LaunchedEffect(selectedFontSize, currentFontSize) {
                                if (selectedFontSize != currentFontSize) {
                                    viewModel.setLyricsFontStyle(
                                        uiState.fontStyle.copy(fontSize = selectedFontSize)
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .nestedScroll(topBarBehaviour.nestedScrollConnection)
                                    .verticalScroll(lyricsScrollState)
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                                    .transformable(
                                        canPan = { false },
                                        state = transformableState,
                                        enabled = !autoScrollStarted
                                    )
                            ) {
                                val lyricsContent = remember(uiState) {
                                    movableContentOf {
                                        Text(
                                            text = uiState.lyrics,
                                            textAlign = if (uiState.fontStyle.alignToStart)
                                                TextAlign.Start
                                            else
                                                TextAlign.Center,
                                            style = uiState.fontStyle.toTextStyle(uiState.themeMode),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                                if (autoScrollStarted) {
                                    lyricsContent()
                                } else {
                                    SelectionContainer {
                                        lyricsContent()
                                    }
                                }
                            }
                        }
                        var autoScrollSpeed by rememberSaveable { mutableFloatStateOf(1f) }
                        val scrollInProgress = lyricsScrollState.isScrollInProgress
                        // Top bar doesn't collapse on manual (auto) scroll, so do it here
                        LaunchedEffect(autoScrollStarted) {
                            if (!autoScrollStarted) return@LaunchedEffect
                            with(topBarBehaviour) {
                                if (state.collapsedFraction < 0.99f) {
                                    animate(
                                        initialValue = state.heightOffset,
                                        targetValue = state.heightOffsetLimit,
                                        animationSpec = spring(stiffness = Spring.StiffnessMedium)
                                    ) { value, _ ->
                                        state.heightOffset = value
                                    }
                                    state.contentOffset = state.heightOffsetLimit
                                }
                            }
                        }
                        LaunchedEffect(
                            autoScrollStarted,
                            autoScrollSpeed,
                            scrollInProgress,
                            uiState.fontStyle,
                        ) {
                            if (autoScrollStarted) {
                                val scrollFraction =
                                    1 - lyricsScrollState.value.toFloat() / lyricsScrollState.maxValue
                                // 3.5 min default (210 seconds), adjust in 1.75..7 min
                                val trackDurationMs = uiState.trackDurationMs ?: 210_000
                                val scrollDurationMs = trackDurationMs / autoScrollSpeed * scrollFraction
                                lyricsScrollState.animateScrollTo(
                                    value = lyricsScrollState.maxValue,
                                    animationSpec = tween(
                                        durationMillis = scrollDurationMs.toInt(),
                                        easing = LinearEasing
                                    )
                                )
                                // reset on finish
                                autoScrollStarted = false
                            }
                        }
                        AnimatedVisibility(
                            visible = autoScrollPanelVisible,
                            enter = fadeIn() + slideInHorizontally { fillWidth -> fillWidth },
                            exit = fadeOut() + slideOutHorizontally { fillWidth -> fillWidth },
                            modifier = Modifier.align(Alignment.BottomEnd)
                        ) {
                            AutoScrollPanel(
                                modifier = Modifier.padding(16.dp),
                                scrollSpeed = autoScrollSpeed,
                                onScrollSpeedChange = { newValue ->
                                    autoScrollSpeed = newValue
                                    requestAutoScrollPanel()
                                },
                                onStopScrollClick = { autoScrollStarted = false }
                            )
                        }
                    }
                }
                if (showFontStyleBottomSheet) {
                    FontStyleBottomSheet(
                        sheetState = fontStyleBottomSheetState,
                        fontStyle = uiState.fontStyle,
                        onFontStyleChanged = viewModel::setLyricsFontStyle,
                        onDismissClick = ::hideFontStyleBottomSheet
                    )
                }
            }
        }
    }
}

@Stable
private fun FontSize.toZoomValue() = when (this) {
    FontSize.Small -> 0.6f
    FontSize.Normal -> 1f
    FontSize.Large -> 1.4f
    FontSize.Huge -> 1.8f
}

@Stable
private fun getFontSize(zoomValue: Float) = when (zoomValue) {
    in Float.MIN_VALUE..0.8f -> FontSize.Small
    in 0.8f..1.2f -> FontSize.Normal
    in 1.2f..1.6f -> FontSize.Large
    in 1.6f..Float.MAX_VALUE -> FontSize.Huge
    else -> FontSize.Normal
}

@Composable
private fun UserPreferences.LyricsFontStyle.toTextStyle(themeMode: ThemeMode): TextStyle {
    return when (fontSize) {
        FontSize.Small -> MaterialTheme.typography.bodyMedium
        FontSize.Normal -> MaterialTheme.typography.bodyLarge
        FontSize.Large -> MaterialTheme.typography.titleLarge
        FontSize.Huge -> MaterialTheme.typography.headlineMedium
    }.copy(
        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
        color = if (isHighContrast) {
            when (themeMode) {
                ThemeMode.FollowSystem -> if (isSystemInDarkTheme()) Color.White else Color.Black
                ThemeMode.AlwaysLight -> Color.Black
                ThemeMode.AlwaysDark -> Color.White
            }
        } else {
            Color.Unspecified
        }
    )
}