package com.mrsep.musicrecognizer.feature.recognition.widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionResult
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionStatus
import com.mrsep.musicrecognizer.feature.recognition.R
import com.mrsep.musicrecognizer.feature.recognition.widget.WidgetUiState
import com.mrsep.musicrecognizer.feature.recognition.widget.ui.RecognitionWidgetLayout.Companion.shouldIncludeFontPadding
import com.mrsep.musicrecognizer.feature.recognition.widget.ui.RecognitionWidgetLayout.Companion.squareWidgetBorderWidth
import com.mrsep.musicrecognizer.feature.recognition.widget.ui.RecognitionWidgetLayout.Companion.subtitleTextSize
import com.mrsep.musicrecognizer.feature.recognition.widget.ui.RecognitionWidgetLayout.Companion.subtitleTopPadding
import com.mrsep.musicrecognizer.feature.recognition.widget.ui.RecognitionWidgetLayout.Companion.titleTextSize
import com.mrsep.musicrecognizer.feature.recognition.widget.util.FontUtils.measureTextExtraPaddings
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR

@Composable
internal fun SquareLayoutContent(
    layout: RecognitionWidgetLayout.Square,
    uiState: WidgetUiState,
    onLaunchRecognition: Action,
    onCancelRecognition: Action,
    onWidgetClick: Action,
) {
    // Clickable modifier affects the shape of widget during smooth transition,
    // it's better when it is not placed on root view, so wrap it in extra box
    Box(
        modifier = GlanceModifier
            .size(layout.widgetSize)
            .roundedWidgetBackground(
                transparent = squareWidgetBorderWidth == 0.dp && uiState.artwork != null
            ),
    ) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .clickable(rippleOverride = -1, onClick = onWidgetClick)
                .padding(squareWidgetBorderWidth),
            contentAlignment = Alignment.Center,
        ) {
            when (val status = uiState.status) {
                RecognitionStatus.Ready,
                is RecognitionStatus.Recognizing,
                -> ButtonWithStatus(
                    uiState = uiState,
                    onLaunchRecognition = onLaunchRecognition,
                    onCancelRecognition = onCancelRecognition,
                    scaledButtonSize = layout.recognitionButtonMaxSize,
                )

                is RecognitionStatus.Done -> {
                    when (val result = status.result) {
                        is RecognitionResult.Error,
                        is RecognitionResult.NoMatches,
                        is RecognitionResult.ScheduledOffline,
                        -> ButtonWithStatus(
                            uiState = uiState,
                            onLaunchRecognition = onLaunchRecognition,
                            onCancelRecognition = onCancelRecognition,
                            scaledButtonSize = layout.recognitionButtonMaxSize,
                        )

                        is RecognitionResult.Success -> Box(
                            modifier = GlanceModifier.size(layout.artworkSize),
                            contentAlignment = Alignment.BottomStart
                        ) {
                            if (uiState.artwork != null) {
                                Image(
                                    provider = ImageProvider(uiState.artwork),
                                    contentDescription = null,
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Box(
                                    modifier = GlanceModifier
                                        .size(layout.artworkSize)
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        provider = ImageProvider(UiR.drawable.outline_album_fill1_24),
                                        contentDescription = null,
                                        contentScale = ContentScale.Fit,
                                        modifier = GlanceModifier.fillMaxSize(),
                                        colorFilter = ColorFilter.tint(GlanceTheme.colors.outline)
                                    )
                                }
                            }
                            if (layout.artworkStyle.bottomFading) {
                                Image(
                                    provider = ImageProvider(R.drawable.widget_artwork_fade_gradient),
                                    contentDescription = null,
                                    contentScale = ContentScale.Fit,
                                    modifier = GlanceModifier.fillMaxSize(),
                                )
                            }
                            TrackInfoWithButton(
                                title = result.track.title,
                                artist = result.track.artist,
                                onLaunchRecognition = onLaunchRecognition,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackInfoWithButton(
    title: String,
    artist: String,
    onLaunchRecognition: Action,
) {
    val context = LocalContext.current
    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = GlanceModifier.defaultWeight()
        ) {
            Text(
                text = title,
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = titleTextSize,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1,
                modifier = GlanceModifier.fillMaxWidth()
            )
            Spacer(GlanceModifier.height(subtitleTopPadding - 2.dp))
            Text(
                text = artist,
                style = TextStyle(
                    color = ColorProvider(Color(0.9f, 0.9f, 0.9f)),
                    fontSize = subtitleTextSize,
                    fontWeight = FontWeight.Normal
                ),
                maxLines = 1,
                modifier = GlanceModifier.fillMaxWidth()
            )
            if (shouldIncludeFontPadding) {
                val paddingForCentering = measureTextExtraPaddings(context, titleTextSize).first -
                        measureTextExtraPaddings(context, subtitleTextSize).second
                Spacer(GlanceModifier.height(paddingForCentering))
            }
        }
        Spacer(GlanceModifier.width(12.dp))
        Box(
            modifier = GlanceModifier
                .clickable(
                    rippleOverride = -1,
                    onClick = onLaunchRecognition
                )
        ) {
            RecognitionButtonContent(
                isRecognizing = false,
                contentSize = 48.dp,
                filledStyle = true,
                contentDescription = context.getString(StringsR.string.action_recognize)
            )
        }
    }
}

@Composable
private fun ButtonWithStatus(
    uiState: WidgetUiState,
    onLaunchRecognition: Action,
    onCancelRecognition: Action,
    scaledButtonSize: Dp,
) {
    val context = LocalContext.current
    Column(
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        AnimatedRecognitionButton(
            isRecognizing = uiState.status is RecognitionStatus.Recognizing,
            onLaunchRecognition = onLaunchRecognition,
            onCancelRecognition = onCancelRecognition,
            filledStyle = true,
            scaledButtonSize = scaledButtonSize
        )
        Spacer(GlanceModifier.height(scaledButtonSize / 3))
        Text(
            text = context.getWidgetTitleForStatus(uiState.status),
            style = TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontSize = titleTextSize,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 2,
            modifier = GlanceModifier.padding(horizontal = 8.dp)
        )
    }
}
