package com.mrsep.musicrecognizer.feature.recognition.widget.ui

import android.widget.RemoteViews
import androidx.compose.runtime.Composable
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
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.size
import androidx.glance.layout.width
import com.mrsep.musicrecognizer.feature.recognition.R
import com.mrsep.musicrecognizer.feature.recognition.widget.ui.RecognitionWidgetLayout.Companion.getButtonSectionWidth
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR

@Composable
internal fun WidgetRecognizeButton(
    isNarrowLayout: Boolean,
    isRecognizing: Boolean,
    onLaunchRecognition: Action,
    onCancelRecognition: Action
) {
    val context = LocalContext.current
    Box(
        modifier = GlanceModifier
            .width(getButtonSectionWidth(isNarrowLayout))
            .fillMaxHeight()
            .clickable(
                rippleOverride = -1,
                onClick = if (isRecognizing) onCancelRecognition else onLaunchRecognition
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isNarrowLayout) {
            Box(
                modifier = GlanceModifier
                    .size(36.dp)
                    .background(ImageProvider(R.drawable.widget_recognition_button_shape)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(UiR.drawable.outline_lines_48),
                    contentDescription = context.getString(StringsR.string.recognize),
                    modifier = GlanceModifier.size(24.dp),
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface)
                )
            }
        } else {
            if (isRecognizing) {
                AndroidRemoteViews(
                    remoteViews = RemoteViews(
                        context.packageName,
                        R.layout.widget_button_animated
                    ),
                )
            } else {
                Box(
                    modifier = GlanceModifier
                        .size(48.dp)
                        .background(ImageProvider(R.drawable.widget_recognition_button_shape)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(UiR.drawable.outline_lines_48),
                        contentDescription = context.getString(StringsR.string.recognize),
                        modifier = GlanceModifier.size(32.dp),
                        colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface)
                    )
                }
            }
        }
    }
}
