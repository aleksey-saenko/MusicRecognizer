package com.mrsep.musicrecognizer.feature.recognition.widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceModifier
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.padding
import androidx.glance.layout.width
import com.mrsep.musicrecognizer.feature.recognition.widget.ui.RecognitionWidgetLayout.Companion.dividerVerticalPadding
import com.mrsep.musicrecognizer.feature.recognition.widget.ui.RecognitionWidgetLayout.Companion.dividerWidth

@Composable
internal fun VerticalDivider() {
    Box(
        modifier = GlanceModifier
            .fillMaxHeight()
            .padding(vertical = dividerVerticalPadding)
    ) {
        Box(
            modifier = GlanceModifier
                .fillMaxHeight()
                .width(dividerWidth)
                .background(
                    ColorProvider(
                        day = Color.Black.copy(0.2f),
                        night = Color.White.copy(0.2f)
                    )
                ),
            content = {}
        )
    }
}
