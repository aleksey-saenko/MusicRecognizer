package com.mrsep.musicrecognizer.feature.recognition.widget.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
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
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionStatus
import com.mrsep.musicrecognizer.feature.recognition.widget.WidgetUiState
import com.mrsep.musicrecognizer.feature.recognition.widget.ui.RecognitionWidgetLayout.Companion.buttonScaleFactor
import com.mrsep.musicrecognizer.feature.recognition.widget.ui.RecognitionWidgetLayout.Companion.circleWidgetBorderWidth
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR

@Composable
internal fun CircleLayoutContent(
    layout: RecognitionWidgetLayout.Circle,
    uiState: WidgetUiState,
    onLaunchRecognition: Action,
    onCancelRecognition: Action,
    onWidgetClick: Action,
) {
    val context = LocalContext.current
    // Clickable modifier affects the shape of widget during smooth transition,
    // it's better when it is not placed on root view, so wrap it in extra box
    Box(
        modifier = GlanceModifier
            .size(layout.widgetSize)
            .circleWidgetBackground(
                widgetSize = layout.widgetSize,
                transparent = circleWidgetBorderWidth == 0.dp && uiState.artwork != null
            ),
    ) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .clickable(rippleOverride = -1, onClick = onWidgetClick)
                .padding(circleWidgetBorderWidth),
            contentAlignment = Alignment.Center,
        ) {
            when (uiState.status) {
                RecognitionStatus.Ready,
                is RecognitionStatus.Recognizing,
                -> {
                    AnimatedRecognitionButton(
                        isRecognizing = uiState.status is RecognitionStatus.Recognizing,
                        onLaunchRecognition = onLaunchRecognition,
                        onCancelRecognition = onCancelRecognition,
                        filledStyle = false,
                        scaledButtonSize = layout.recognitionButtonMaxSize
                    )
                }

                is RecognitionStatus.Done -> {
                    when (uiState.status.result) {
                        is RecognitionResult.Error -> ResultIcon(
                            iconResId = UiR.drawable.rounded_priority_high_48,
                            contentDescriptionResId = StringsR.string.unknown_error,
                            size = layout.recognitionButtonMaxSize / buttonScaleFactor()
                        )

                        is RecognitionResult.NoMatches -> ResultIcon(
                            iconResId = UiR.drawable.rounded_question_mark_48,
                            contentDescriptionResId = StringsR.string.no_matches_found,
                            size = layout.recognitionButtonMaxSize / buttonScaleFactor()
                        )

                        is RecognitionResult.ScheduledOffline -> ResultIcon(
                            iconResId = UiR.drawable.rounded_priority_high_48,
                            contentDescriptionResId = StringsR.string.no_matches_found,
                            size = layout.recognitionButtonMaxSize / buttonScaleFactor()
                        )

                        is RecognitionResult.Success -> if (layout.showArtwork && uiState.artwork != null) {
                            Box { // Somehow this useless Box helps to avoid glitches when changing the size of the widget with artwork. Glance...
                                Image(
                                    provider = ImageProvider(uiState.artwork),
                                    contentDescription = context.getString(StringsR.string.show_track),
                                    contentScale = ContentScale.Fit,
                                    modifier = GlanceModifier.size(layout.artworkSize)
                                )
                            }
                        } else {
                            ResultIcon(
                                iconResId = UiR.drawable.rounded_music_note_48,
                                contentDescriptionResId = StringsR.string.show_track,
                                size = layout.recognitionButtonMaxSize / buttonScaleFactor()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultIcon(
    @DrawableRes iconResId: Int,
    @StringRes contentDescriptionResId: Int,
    size: Dp,
) {
    Image(
        provider = ImageProvider(iconResId),
        contentDescription = LocalContext.current.getString(contentDescriptionResId),
        contentScale = ContentScale.Fit,
        colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface),
        modifier = GlanceModifier.size(size).padding(4.dp)
    )
}
