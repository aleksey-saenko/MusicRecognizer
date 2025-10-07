package com.mrsep.musicrecognizer.feature.recognition.widget.ui

import android.widget.RemoteViews
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
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import com.mrsep.musicrecognizer.feature.recognition.R
import com.mrsep.musicrecognizer.feature.recognition.widget.ui.RecognitionWidgetLayout.Companion.buttonScaleFactor
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR

@Composable
internal fun AnimatedRecognitionButton(
    isRecognizing: Boolean,
    onLaunchRecognition: Action,
    onCancelRecognition: Action,
    filledStyle: Boolean = true,
    scaledButtonSize: Dp,
) {
    val context = LocalContext.current
    val buttonScaleFactor = buttonScaleFactor()
    val contentDescription = context.getString(
        if (isRecognizing) StringsR.string.action_cancel_recognition
        else StringsR.string.action_recognize
    )
    Box(
        modifier = GlanceModifier
            .size(scaledButtonSize)
            .clickable(
                rippleOverride = -1,
                onClick = if (isRecognizing) onCancelRecognition else onLaunchRecognition
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isRecognizing) {
            // Using a hack to make animated button in Glance. Open for refactoring.
            AndroidRemoteViews(
                remoteViews = RemoteViews(
                    context.packageName,
                    R.layout.widget_flipper_container
                ),
                containerViewId = R.id.widget_flipper_container,
                modifier = GlanceModifier.fillMaxSize()
            ) {
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    RecognitionButtonContent(
                        isRecognizing = true,
                        contentSize = scaledButtonSize / buttonScaleFactor,
                        filledStyle = filledStyle,
                        contentDescription = contentDescription,
                    )
                }
                Box {} // Used to change flipper states
            }
        } else {
            RecognitionButtonContent(
                isRecognizing = false,
                contentSize = scaledButtonSize / buttonScaleFactor,
                filledStyle = filledStyle,
                contentDescription = contentDescription,
            )
        }
    }
}

@Composable
internal fun RecognitionButtonContent(
    isRecognizing: Boolean,
    contentSize: Dp,
    filledStyle: Boolean = true,
    contentDescription: String,
) {
    Box(
        modifier = GlanceModifier
            .size(contentSize)
            .semantics { this.contentDescription = contentDescription }
            .then(
                if (filledStyle) {
                    GlanceModifier.background(ImageProvider(R.drawable.widget_recognition_button_shape))
                } else {
                    GlanceModifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            provider = ImageProvider(UiR.drawable.outline_lines_shift1_48),
            contentDescription = null,
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(if (contentSize >= 40.dp) 8.dp else 6.dp),
            colorFilter = if (filledStyle) {
                ColorFilter.tint(GlanceTheme.colors.onPrimary)
            } else {
                if (isRecognizing) {
                    ColorFilter.tint(GlanceTheme.colors.primary)
                } else {
                    ColorFilter.tint(GlanceTheme.colors.onSurface)
                }
            }
        )
    }
}
