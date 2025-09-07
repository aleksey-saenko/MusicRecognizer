package com.mrsep.musicrecognizer.feature.recognition.widget.ui

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import com.mrsep.musicrecognizer.feature.recognition.R

/**
 * Transparent background is used when displaying artwork in full widget size
 * to avoid overlay artifacts on the edges
 */
@Composable
internal fun GlanceModifier.circleWidgetBackground(widgetSize: Dp, transparent: Boolean = false): GlanceModifier = then(
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        GlanceModifier
            .appWidgetBackground()
            .then(
                if (transparent) {
                    GlanceModifier.background(android.R.color.transparent)
                } else {
                    GlanceModifier.background(GlanceTheme.colors.widgetBackground)
                }
            )
            .cornerRadius(widgetSize / 2)
    } else {
        GlanceModifier
            .appWidgetBackground()
            .then(
                if (transparent) {
                    GlanceModifier.background(android.R.color.transparent)
                } else {
                    GlanceModifier.background(ImageProvider(R.drawable.widget_circle_background_shape))
                }
            )
    }
)

/**
 * Transparent background is used when displaying artwork in full widget size
 * to avoid overlay artifacts on the edges
 */
@Composable
internal fun GlanceModifier.roundedWidgetBackground(transparent: Boolean = false): GlanceModifier = then(
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        GlanceModifier
            .appWidgetBackground()
            .then(
                if (transparent) {
                    GlanceModifier.background(android.R.color.transparent)
                } else {
                    GlanceModifier.background(GlanceTheme.colors.widgetBackground)
                }
            )
            .cornerRadius(R.dimen.widget_background_radius)
    } else {
        GlanceModifier
            .appWidgetBackground()
            .then(
                if (transparent) {
                    GlanceModifier.background(android.R.color.transparent)
                } else {
                    GlanceModifier.background(ImageProvider(R.drawable.widget_background_shape))
                }
            )
    }
)