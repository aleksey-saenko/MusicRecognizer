package com.mrsep.musicrecognizer.feature.recognition.widget.ui

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import com.mrsep.musicrecognizer.feature.recognition.R
import com.mrsep.musicrecognizer.core.ui.R as UiR

@Composable
internal fun ArtworkRoundedPlaceholder(artworkSize: Dp) {
    Box(
        modifier = GlanceModifier
            .then(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val systemInnerRadiusDefined = LocalContext.current.resources
                        .getResourceName(android.R.dimen.system_app_widget_inner_radius) != null
                    GlanceModifier
                        .background(UiR.color.surface_container_highest)
                        .background(R.color.widget_artwork_background)
                        .cornerRadius(
                            if (systemInnerRadiusDefined) {
                                android.R.dimen.system_app_widget_inner_radius
                            } else {
                                R.dimen.widget_inner_radius
                            }
                        )
                } else {
                    GlanceModifier
                        .background(ImageProvider(R.drawable.widget_artwork_shape))
                }
            )
            .size(artworkSize)
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
