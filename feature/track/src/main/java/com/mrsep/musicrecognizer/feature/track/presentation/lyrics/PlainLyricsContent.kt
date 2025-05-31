package com.mrsep.musicrecognizer.feature.track.presentation.lyrics

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrsep.musicrecognizer.core.domain.preferences.LyricsStyle
import com.mrsep.musicrecognizer.core.ui.components.rememberMultiSelectionState
import com.mrsep.musicrecognizer.core.ui.util.copyTextToClipboard
import com.mrsep.musicrecognizer.core.ui.util.shareText
import com.mrsep.musicrecognizer.feature.track.presentation.track.verticalFadingEdges
import kotlinx.coroutines.delay
import kotlin.time.Duration

@Composable
internal fun PlainLyricsContent(
    lyrics: String,
    title: String,
    artist: String,
    artworkUrl: String?,
    trackDuration: Duration?,
    lyricsStyle: LyricsStyle,
    lyricsTextStyle: TextStyle,
    onBackPressed: () -> Unit,
    onShowLyricsStyleSettings: () -> Unit,
    onChangeLyricsStyle: (newStyle: LyricsStyle) -> Unit,
    createSeedColor: Boolean,
    onSeedColorCreated: (Int) -> Unit,
) {
    val context = LocalContext.current
    val view = LocalView.current
    val density = LocalDensity.current

    val lines = remember(lyrics) { lyrics.lines() }
    val multiSelectionState = rememberMultiSelectionState(lines, selectedIds = emptyList<Int>())

    val scrollState = rememberScrollState()
    var autoScrollStarted by rememberSaveable { mutableStateOf(false) }

    BackHandler(
        enabled = multiSelectionState.hasSelected,
        onBack = multiSelectionState::deselectAll
    )

    DisposableEffect(autoScrollStarted) {
        view.keepScreenOn = autoScrollStarted
        onDispose { view.keepScreenOn = false }
    }

    // 0.5f..2f
    var autoScrollSpeed by rememberSaveable { mutableFloatStateOf(1f) }
    LaunchedEffect(
        autoScrollStarted,
        autoScrollSpeed,
        scrollState.isScrollInProgress,
        lyricsStyle,
    ) {
        if (autoScrollStarted) {
            val scrollFraction =
                1 - scrollState.value.toFloat() / scrollState.maxValue
            // 3.5 min default (210 seconds), adjust in 1.75..7 min
            val trackDurationMs = trackDuration?.inWholeMilliseconds ?: 210_000
            val scrollDurationMs = trackDurationMs / autoScrollSpeed * scrollFraction
            scrollState.animateScrollTo(
                value = scrollState.maxValue,
                animationSpec = tween(
                    durationMillis = scrollDurationMs.toInt(),
                    easing = LinearEasing
                )
            )
            autoScrollStarted = false
        }
    }

    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            val updatedStyle by rememberUpdatedState(lyricsStyle)
            LyricsScreenTopBar(
                title = title,
                artist = artist,
                artworkUrl = artworkUrl,
                isScrolled = scrollState.canScrollBackward,
                selectedCount = multiSelectionState.selectedCount,
                onSelectAll = { multiSelectionState.select(List(lines.size) { it }) },
                onDeselectAll = multiSelectionState::deselectAll,
                onBackPressed = onBackPressed,
                onCopyClick = {
                    context.copyTextToClipboard(lines.joinLines(multiSelectionState.getSelected()))
                    multiSelectionState.deselectAll()
                },
                onShareClick = {
                    if (multiSelectionState.hasSelected) {
                        context.shareText(
                            subject = "",
                            body = lines.joinLines(multiSelectionState.getSelected())
                        )
                    } else {
                        val trackInfo = "$title - $artist"
                        context.shareText(
                            subject = trackInfo,
                            body = "$trackInfo\n\n$lyrics"
                        )
                    }
                    multiSelectionState.deselectAll()
                },
                onLyricsStyleClick = onShowLyricsStyleSettings,
                lyricsFontSize = lyricsStyle.fontSize,
                onChangeLyricsFontSize = { newSize ->
                    onChangeLyricsStyle(updatedStyle.copy(fontSize = newSize))
                },
                createSeedColor = createSeedColor,
                onSeedColorCreated = onSeedColorCreated,
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalFadingEdges(
                        topFadeLength = 56.dp,
                        topFadeInitialColor = MaterialTheme.colorScheme.surface
                    )
                    .verticalScroll(scrollState)
                    .padding(
                        start = 8.dp,
                        end = 8.dp,
                        top = if (lines.first().isBlank()) 32.dp else 48.dp,
                        bottom = if (lines.last().isBlank()) 32.dp else 48.dp
                    ),
                verticalArrangement = remember(lyricsTextStyle.fontSize) {
                    with(density) {
                        Arrangement.spacedBy((lyricsTextStyle.fontSize.toDp() - 16.dp).coerceAtLeast(4.dp))
                    }
                },
                horizontalAlignment = when (lyricsTextStyle.textAlign) {
                    TextAlign.Center -> Alignment.CenterHorizontally
                    TextAlign.Start -> Alignment.Start
                    TextAlign.End -> Alignment.End
                    else -> error("Not implemented")
                }
            ) {
                lines.forEachIndexed { index, line ->
                    LyricLine(
                        line = line,
                        style = lyricsTextStyle,
                        isSelected = multiSelectionState.isSelected(index),
                        onClick = {
                            if (multiSelectionState.hasSelected) {
                                multiSelectionState.toggleSelection(index)
                            }
                        },
                        onLongPress = {
                            multiSelectionState.toggleSelection(index)
                        },
                    )
                }
            }
        }

        val autoScrollAvailable by remember {
            derivedStateOf { with(scrollState) { canScrollForward || canScrollBackward } }
        }
        val isLyricsDragged by scrollState.interactionSource.collectIsDraggedAsState()
        var showAutoScrollPanel by remember { mutableStateOf(autoScrollAvailable) }
        LaunchedEffect(
            multiSelectionState.hasSelected,
            autoScrollAvailable,
            autoScrollStarted,
            isLyricsDragged,
            scrollState.canScrollBackward,
            autoScrollSpeed,
        ) {
            when {
                multiSelectionState.hasSelected || !autoScrollAvailable -> {
                    showAutoScrollPanel = false
                }

                !autoScrollStarted -> {
                    showAutoScrollPanel = !scrollState.canScrollBackward
                }

                isLyricsDragged -> {
                    showAutoScrollPanel = true
                }

                else -> {
                    delay(3000)
                    showAutoScrollPanel = false
                }
            }
        }

        AnimatedVisibility(
            visible = showAutoScrollPanel,
            enter = slideInVertically { fillHeight -> fillHeight },
            exit = slideOutVertically { fillHeight -> fillHeight },
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            AutoScrollToolbarVertical(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .safeDrawingPadding(),
                isStarted = autoScrollStarted,
                onStartScrollClick = { autoScrollStarted = true },
                onStopScrollClick = { autoScrollStarted = false },
                scrollSpeed = autoScrollSpeed,
                onScrollSpeedChange = { newValue -> autoScrollSpeed = newValue },
            )
        }
    }
}

@Composable
private fun LyricLine(
    modifier: Modifier = Modifier,
    line: String,
    style: TextStyle,
    selectionContainerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
) {
    val selectionContainerAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = spring(stiffness = ColorAnimationStiffness),
        label = "selectionContainerAlpha"
    )
    Text(
        text = line,
        style = style,
        modifier = modifier
            .drawBehind {
                drawRoundRect(
                    color = selectionContainerColor,
                    alpha = selectionContainerAlpha,
                    cornerRadius = 8.dp.toPx().run(::CornerRadius),
                )
            }
            .combinedClickable(
                interactionSource = null,
                indication = null,
                onClick = onClick,
                onLongClick = onLongPress
            )
            .padding(if (style.fontSize > 14.sp) 8.dp else 6.dp)
    )
}
