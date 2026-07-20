package com.mrsep.musicrecognizer.feature.recognition.service.floating.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.materialkolor.ktx.toColor
import com.materialkolor.ktx.toHct
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionResult
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionStatus
import com.mrsep.musicrecognizer.core.domain.track.model.SyncedLyrics
import com.mrsep.musicrecognizer.core.ui.theme.MusicRecognizerTheme
import com.mrsep.musicrecognizer.core.ui.theme.SwitchingMusicRecognizerTheme
import com.mrsep.musicrecognizer.core.ui.util.copyTextToClipboard
import com.mrsep.musicrecognizer.feature.recognition.DeeplinkRouter
import com.mrsep.musicrecognizer.feature.recognition.service.floating.FloatingWindowSharedModel
import com.mrsep.musicrecognizer.feature.recognition.service.floating.FloatingWindowUiState
import com.mrsep.musicrecognizer.feature.recognition.service.floating.core.LocalFloatingWindow
import com.mrsep.musicrecognizer.feature.recognition.service.floating.ui.components.FloatingSyncedLyrics
import com.mrsep.musicrecognizer.feature.recognition.service.floating.ui.components.rememberSegmentedTailShapes
import com.mrsep.musicrecognizer.feature.recognition.service.floating.ui.components.shouldShowFloatingLyrics
import com.mrsep.musicrecognizer.feature.recognition.widget.ui.getWidgetSubtitleForStatus
import com.mrsep.musicrecognizer.feature.recognition.widget.ui.getWidgetTitleForStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR

internal val GapBetweenWindows = 8.dp
private val CloseButtonFixedWidth = 56.dp
private val ContentFixedWidth = 176.dp
private const val SubContentColorAlpha = 0.8f

private val TrackInfoMinTimeout = 7.seconds
private val TrackInfoExtendedTimeout = 15.seconds

private enum class TrackContentMode { TrackInfo, Lyrics }

@Composable
internal fun ExtraWindow(
    sharedModel: FloatingWindowSharedModel,
    deeplinkRouter: DeeplinkRouter,
) {
    val context = LocalContext.current
    val window = LocalFloatingWindow.current
    val uiState by sharedModel.uiState.collectAsStateWithLifecycle()

    val isTouchable = uiState.recognitionStatus is RecognitionStatus.Done
    LaunchedEffect(isTouchable) {
        window.setTouchable(isTouchable)
    }

    LaunchedEffect(Unit) {
        try {
            snapshotFlow { uiState.recognitionStatus }
                .filter { it !is RecognitionStatus.Done }
                .collect { sharedModel.isShowingLyrics.value = false }
        } finally {
            sharedModel.isShowingLyrics.value = false
        }
    }

    MusicRecognizerTheme {
        AnimatedContent(
            targetState = uiState,
            transitionSpec = {
                floatingWindowTransform(sharedModel.isLeftAnchored.value)
            },
            contentKey = { state -> state.recognitionStatus },
            contentAlignment = Alignment.Center,
            modifier = Modifier
        ) { state ->
            when (val status = state.recognitionStatus) {
                RecognitionStatus.Ready,
                is RecognitionStatus.Recognizing -> {
                    // "Anchor pixel" to prevent WindowManager from destroying the 0x0 Surface
                    // Required for AnimatedContent to have a valid canvas for enter/exit animations
                    // Touches are passed through to the OS by dynamically setting FLAG_NOT_TOUCHABLE
                    Spacer(modifier = Modifier.size(1.dp))
                }

                is RecognitionStatus.Done -> {
                    when (val result = status.result) {
                        is RecognitionResult.Success -> {

                            val currentTrack = state.currentTrack ?: result.track
                            val isLyricsFetcherRunning = state.isLyricsFetcherRunning

                            var contentMode by remember { mutableStateOf(TrackContentMode.TrackInfo) }
                            val firstShowMark = remember { TimeSource.Monotonic.markNow() }

                            // It's expected that the lyrics should start from zero
                            val syncedLyrics = remember(currentTrack.lyrics) {
                                (currentTrack.lyrics as? SyncedLyrics)?.run {
                                    val originalLines = lines
                                    val firstLine = originalLines.firstOrNull()
                                    when {
                                        firstLine == null || firstLine.timestamp == Duration.ZERO -> this
                                        else -> SyncedLyrics(
                                            buildList(capacity = originalLines.size + 1) {
                                                add(SyncedLyrics.Line(Duration.ZERO, ""))
                                                addAll(originalLines)
                                            }
                                        )
                                    }
                                }
                            }

                            LaunchedEffect(
                                contentMode,
                                syncedLyrics,
                                isLyricsFetcherRunning,
                                currentTrack.duration,
                                currentTrack.recognizedAt,
                                currentTrack.recognitionDate,
                            ) {
                                if (contentMode != TrackContentMode.TrackInfo) return@LaunchedEffect

                                if (syncedLyrics != null) {
                                    val trackInfoRemainingTime = TrackInfoMinTimeout - firstShowMark.elapsedNow()
                                    delay(trackInfoRemainingTime)
                                    val shouldShowLyrics = shouldShowFloatingLyrics(
                                        lyrics = syncedLyrics,
                                        trackDuration = currentTrack.duration,
                                        recognizedAt = currentTrack.recognizedAt,
                                        recognitionDate = currentTrack.recognitionDate,
                                        threshold = 5.seconds
                                    )
                                    if (shouldShowLyrics) {
                                        contentMode = TrackContentMode.Lyrics
                                    } else {
                                        sharedModel.dismissRecognitionResult()
                                    }
                                } else if (isLyricsFetcherRunning) {
                                    val trackInfoRemainingTime = TrackInfoExtendedTimeout - firstShowMark.elapsedNow()
                                    delay(trackInfoRemainingTime)
                                    sharedModel.dismissRecognitionResult()
                                } else {
                                    val trackInfoRemainingTime = TrackInfoMinTimeout - firstShowMark.elapsedNow()
                                    delay(trackInfoRemainingTime)
                                    sharedModel.dismissRecognitionResult()
                                }
                            }

                            LaunchedEffect(contentMode) {
                                sharedModel.isShowingLyrics.value = when (contentMode) {
                                    TrackContentMode.TrackInfo -> false
                                    TrackContentMode.Lyrics -> true
                                }
                            }

                            SwitchingMusicRecognizerTheme(
                                seedColor = currentTrack.properties.themeSeedColor?.run(::Color),
                                style = ThemePaletteStyle,
                            ) {
                                AnimatedContent(
                                    targetState = contentMode,
                                    transitionSpec = trackContentTransform,
                                ) { contentMode ->
                                    when (contentMode) {
                                        TrackContentMode.TrackInfo -> {
                                            ExtraWindowContainer(
                                                isLeftAnchored = sharedModel.isLeftAnchored.value,
                                                onCloseClick = {
                                                    sharedModel.dismissRecognitionResult()
                                                },
                                                onCloseClickLabel = stringResource(StringsR.string.close),
                                                onContentClick = {
                                                    val intent = deeplinkRouter.getDeepLinkIntentToTrack(result.track.id)
                                                    context.startActivity(intent)
                                                    sharedModel.dismissRecognitionResult()
                                                },
                                                onContentClickLabel = stringResource(StringsR.string.show),
                                                onContentLongClick = {
                                                    with(result.track) {
                                                        context.copyTextToClipboard("$title - $artist")
                                                    }
                                                },
                                                onContentLongClickLabel = stringResource(StringsR.string.copy)
                                            ) {
                                                TextInfo(
                                                    title = currentTrack.title,
                                                    subtitle = currentTrack.artist,
                                                    titleMaxLines = 2,
                                                    subtitleMaxLines = 1,
                                                    modifier = Modifier
                                                        .padding(16.dp)
                                                        .width(ContentFixedWidth)
                                                )
                                            }
                                        }

                                        TrackContentMode.Lyrics -> {
                                            ExtraWindowContainer(
                                                isLeftAnchored = sharedModel.isLeftAnchored.value,
                                                onCloseClick = {
                                                    sharedModel.dismissRecognitionResult()
                                                },
                                                onCloseClickLabel = stringResource(StringsR.string.close),
                                                onContentClick = {
                                                    val intent = deeplinkRouter.getDeepLinkIntentToLyrics(result.track.id)
                                                    context.startActivity(intent)
                                                    sharedModel.dismissRecognitionResult()
                                                },
                                                onContentClickLabel = stringResource(StringsR.string.show),
                                            ) {
                                                if (syncedLyrics != null) {
                                                    FloatingSyncedLyrics(
                                                        lyrics = syncedLyrics,
                                                        trackDuration = currentTrack.duration,
                                                        recognizedAt = currentTrack.recognizedAt,
                                                        recognitionDate = currentTrack.recognitionDate,
                                                        onPlaybackStopped = {
                                                            sharedModel.dismissRecognitionResult()
                                                        },
                                                        verticalPadding = 16.dp,
                                                        modifier = Modifier
                                                            .padding(horizontal = 16.dp)
                                                            .width(ContentFixedWidth),
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        is RecognitionResult.NoMatches,
                        is RecognitionResult.ScheduledOffline,
                        is RecognitionResult.Error -> {
                            val title = context.getWidgetTitleForStatus(status)
                            val subtitle = context.getWidgetSubtitleForStatus(status)
                            ExtraWindowContainer(
                                isLeftAnchored = sharedModel.isLeftAnchored.value,
                                onCloseClick = {
                                    sharedModel.dismissRecognitionResult()
                                },
                                onCloseClickLabel = stringResource(StringsR.string.close),
                                onContentClick = {
                                    if (result is RecognitionResult.ScheduledOffline) {
                                        val intent = deeplinkRouter.getDeepLinkIntentToQueue()
                                        context.startActivity(intent)
                                    }
                                    sharedModel.dismissRecognitionResult()
                                },
                                onContentClickLabel = stringResource(StringsR.string.show),
                            ) {
                                TextInfo(
                                    title = title,
                                    subtitle = subtitle,
                                    titleMaxLines = 1,
                                    subtitleMaxLines = 2,
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .width(ContentFixedWidth)
                                )
                            }
                        }
                    }
                }
            }
        }

    }
}

@Composable
private fun ExtraWindowContainer(
    modifier: Modifier = Modifier,
    isLeftAnchored: Boolean,
    onCloseClick: () -> Unit,
    onCloseClickLabel: String,
    onContentClick: () -> Unit,
    onContentClickLabel: String,
    onContentLongClick: (() -> Unit)? = null,
    onContentLongClickLabel: String? = null,
    content: @Composable (BoxScope.() -> Unit),
) {
    val tailDepth = 10.dp
    val extraTailPadding = 4.dp
    val shapes = rememberSegmentedTailShapes(
        isLeftAnchored = isLeftAnchored,
        cornerRadius = 18.dp,
        tailDepth = tailDepth,
        tailHeight = 24.dp,
        tailTipRadius = 3.dp,
    )
    val isDarkTheme = isSystemInDarkTheme()
    val baseContainerColor = MaterialTheme.colorScheme.primaryContainer
    val baseContainerContentColor = MaterialTheme.colorScheme.onPrimaryContainer
    val closeButtonContainerColor = remember(baseContainerColor, isDarkTheme) {
        val toneShift = if (isDarkTheme) 4.0 else -1.6
        val hct = baseContainerColor.toHct()
        val targetTone = (hct.tone + toneShift).coerceIn(0.0, 100.0)
        hct.withTone(targetTone).toColor()
    }
    Surface(
        modifier = modifier,
        color = baseContainerColor,
        contentColor = baseContainerContentColor,
        shape = shapes.commonShape
    ) {
        val actualLayoutDirection = LocalLayoutDirection.current
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(
                modifier = Modifier
                    .height(IntrinsicSize.Min)
            ) {
                if (!isLeftAnchored) {
                    CloseButton(
                        modifier = Modifier
                            .width(CloseButtonFixedWidth)
                            .fillMaxHeight(),
                        containerColor = closeButtonContainerColor,
                        shape = shapes.leftButtonShape,
                        onClick = onCloseClick,
                        onClickLabel = onCloseClickLabel,
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(shape = if (isLeftAnchored) shapes.leftButtonShape else shapes.rightButtonShape)
                        .combinedClickable(
                            onClick = onContentClick,
                            onClickLabel = onContentClickLabel,
                            onLongClick = onContentLongClick,
                            onLongClickLabel = onContentLongClickLabel,
                            role = Role.Button
                        )
                        .run {
                            if (isLeftAnchored)
                                absolutePadding(left = tailDepth + extraTailPadding)
                            else
                                absolutePadding(right = tailDepth + extraTailPadding)
                        }
                        .fillMaxHeight(),
                ) {
                    CompositionLocalProvider(LocalLayoutDirection provides actualLayoutDirection) {
                        content()
                    }
                }

                if (isLeftAnchored) {
                    CloseButton(
                        modifier = Modifier
                            .width(CloseButtonFixedWidth)
                            .fillMaxHeight(),
                        containerColor = closeButtonContainerColor,
                        shape = shapes.rightButtonShape,
                        onClick = onCloseClick,
                        onClickLabel = onCloseClickLabel,
                    )
                }
            }
        }
    }
}

@Composable
private fun CloseButton(
    modifier: Modifier = Modifier,
    containerColor: Color,
    shape: Shape,
    onClick: () -> Unit,
    onClickLabel: String,
) {
    Box(
        modifier = modifier
            .background(color = containerColor, shape = shape)
            .clip(shape)
            .clickable(
                onClick = onClick,
                onClickLabel = onClickLabel,
                role = Role.Button
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(UiR.drawable.rounded_close_24),
            contentDescription = stringResource(StringsR.string.close),
            tint = LocalContentColor.current.copy(SubContentColorAlpha),
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun TextInfo(
    modifier: Modifier = Modifier,
    title: String,
    titleMaxLines: Int,
    subtitle: String?,
    subtitleMaxLines: Int?,
    textAlignment: TextAlign = TextAlign.Start,
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            maxLines = titleMaxLines,
            style = MaterialTheme.typography.titleMedium,
            color = LocalContentColor.current,
            textAlign = textAlignment,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
        if (subtitle != null && subtitleMaxLines != null) {
            Spacer(Modifier.height(3.dp))
            Text(
                text = subtitle,
                maxLines = subtitleMaxLines,
                style = MaterialTheme.typography.bodyMedium,
                color = LocalContentColor.current.copy(SubContentColorAlpha),
                textAlign = textAlignment,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private const val EXTRA_WINDOW_ANIMATION_DURATION_MS = 400
internal const val TRACK_CONTENT_ANIMATION_DURATION_MS = 250

private val trackContentTransform: AnimatedContentTransitionScope<TrackContentMode>.() -> ContentTransform = {
    // The old content exits smoothly, the size snaps, then the new content enters smoothly
    (fadeIn(
        animationSpec = tween(
            durationMillis = TRACK_CONTENT_ANIMATION_DURATION_MS,
            delayMillis = TRACK_CONTENT_ANIMATION_DURATION_MS
        )
    ) + scaleIn(
        initialScale = 0.7f,
        animationSpec = tween(
            durationMillis = TRACK_CONTENT_ANIMATION_DURATION_MS,
            delayMillis = TRACK_CONTENT_ANIMATION_DURATION_MS
        )
    )).togetherWith(
        fadeOut(
            animationSpec = tween(durationMillis = TRACK_CONTENT_ANIMATION_DURATION_MS)
        ) + scaleOut(
            targetScale = 0.7f,
            animationSpec = tween(durationMillis = TRACK_CONTENT_ANIMATION_DURATION_MS)
        )
    ).using(
        SizeTransform { _, _ -> snap(delayMillis = TRACK_CONTENT_ANIMATION_DURATION_MS) }
    )
}

private fun AnimatedContentTransitionScope<TrackContentMode>.trackContentTransform(
    isLeftAnchored: Boolean,
): ContentTransform {
    val slideInDirection = if (isLeftAnchored) {
        AnimatedContentTransitionScope.SlideDirection.Right
    } else {
        AnimatedContentTransitionScope.SlideDirection.Left
    }
    val slideOutDirection = if (isLeftAnchored) {
        AnimatedContentTransitionScope.SlideDirection.Left
    } else {
        AnimatedContentTransitionScope.SlideDirection.Right
    }
    // The old content exits smoothly, the size snaps, then the new content enters smoothly
    return (fadeIn(
        animationSpec = tween(
            durationMillis = EXTRA_WINDOW_ANIMATION_DURATION_MS,
            delayMillis = EXTRA_WINDOW_ANIMATION_DURATION_MS
        )
    ) + scaleIn(
        initialScale = 0f,
        animationSpec = tween(
            durationMillis = EXTRA_WINDOW_ANIMATION_DURATION_MS,
            delayMillis = EXTRA_WINDOW_ANIMATION_DURATION_MS
        )
    ) + slideIntoContainer(
        towards = slideInDirection,
        animationSpec = tween(
            durationMillis = EXTRA_WINDOW_ANIMATION_DURATION_MS,
            delayMillis = EXTRA_WINDOW_ANIMATION_DURATION_MS
        ),
        initialOffset = { it / 2 }
    )).togetherWith(
        fadeOut(
            animationSpec = tween(durationMillis = EXTRA_WINDOW_ANIMATION_DURATION_MS)
        ) + scaleOut(
            targetScale = 0f,
            animationSpec = tween(durationMillis = EXTRA_WINDOW_ANIMATION_DURATION_MS)
        ) + slideOutOfContainer(
            towards = slideOutDirection,
            animationSpec = tween(durationMillis = EXTRA_WINDOW_ANIMATION_DURATION_MS),
            targetOffset = { it / 2 }
        )
    ).using(
        SizeTransform { _, _ -> snap(delayMillis = EXTRA_WINDOW_ANIMATION_DURATION_MS) }
    )
}

private fun AnimatedContentTransitionScope<FloatingWindowUiState>.floatingWindowTransform(
    isLeftAnchored: Boolean,
): ContentTransform {
    val slideInDirection = if (isLeftAnchored) {
        AnimatedContentTransitionScope.SlideDirection.Right
    } else {
        AnimatedContentTransitionScope.SlideDirection.Left
    }
    val slideOutDirection = if (isLeftAnchored) {
        AnimatedContentTransitionScope.SlideDirection.Left
    } else {
        AnimatedContentTransitionScope.SlideDirection.Right
    }

    val initialStatus = initialState.recognitionStatus
    val targetStatus = targetState.recognitionStatus

    return when {
        // Ready <-> Recognizing: Instant transition
        (initialStatus is RecognitionStatus.Ready || initialStatus is RecognitionStatus.Recognizing) &&
                (targetStatus is RecognitionStatus.Ready || targetStatus is RecognitionStatus.Recognizing) -> {
            EnterTransition.None
                .togetherWith(ExitTransition.None)
                .using(SizeTransform { _, _ -> snap() })
        }

        // Done -> non-Done: The Done state exits smoothly, the size snaps, then the new content appears instantly
        initialStatus is RecognitionStatus.Done &&
                targetStatus !is RecognitionStatus.Done -> {
            EnterTransition.None
                .togetherWith(
                    fadeOut(
                        animationSpec = tween(durationMillis = EXTRA_WINDOW_ANIMATION_DURATION_MS)
                    ) + scaleOut(
                        targetScale = 0f,
                        animationSpec = tween(durationMillis = EXTRA_WINDOW_ANIMATION_DURATION_MS)
                    ) + slideOutOfContainer(
                        towards = slideOutDirection,
                        animationSpec = tween(durationMillis = EXTRA_WINDOW_ANIMATION_DURATION_MS),
                        targetOffset = { it }
                    )
                )
                .using(
                    SizeTransform { _, _ -> snap(delayMillis = EXTRA_WINDOW_ANIMATION_DURATION_MS) }
                )
        }

        // non-Done -> Done: The old content disappears instantly, the size snaps, then the Done state enters smoothly
        targetStatus is RecognitionStatus.Done &&
                initialStatus !is RecognitionStatus.Done -> {
            (fadeIn(
                animationSpec = tween(durationMillis = EXTRA_WINDOW_ANIMATION_DURATION_MS)
            ) + scaleIn(
                initialScale = 0f,
                animationSpec = tween(durationMillis = EXTRA_WINDOW_ANIMATION_DURATION_MS)
            ) + slideIntoContainer(
                towards = slideInDirection,
                animationSpec = tween(durationMillis = EXTRA_WINDOW_ANIMATION_DURATION_MS),
                initialOffset = { it }
            ))
                .togetherWith(ExitTransition.None)
                .using(SizeTransform { _, _ -> snap() })
        }

        else -> {
            EnterTransition.None
                .togetherWith(ExitTransition.None)
                .using(SizeTransform { _, _ -> snap() })
        }
    }
}
