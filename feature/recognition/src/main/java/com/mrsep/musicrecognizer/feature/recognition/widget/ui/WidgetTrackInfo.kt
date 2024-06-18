package com.mrsep.musicrecognizer.feature.recognition.widget.ui

import android.graphics.Bitmap
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.RowScope
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.mrsep.musicrecognizer.feature.recognition.R
import com.mrsep.musicrecognizer.feature.recognition.domain.model.Track
import com.mrsep.musicrecognizer.feature.recognition.widget.ui.RecognitionWidgetLayout.Companion.artworkToTextPadding
import com.mrsep.musicrecognizer.feature.recognition.widget.ui.RecognitionWidgetLayout.Companion.contentPadding
import com.mrsep.musicrecognizer.feature.recognition.widget.ui.RecognitionWidgetLayout.Companion.subtitleTextSize
import com.mrsep.musicrecognizer.feature.recognition.widget.ui.RecognitionWidgetLayout.Companion.subtitleTopPadding
import com.mrsep.musicrecognizer.feature.recognition.widget.ui.RecognitionWidgetLayout.Companion.titleTextSize
import com.mrsep.musicrecognizer.core.ui.R as UiR

// Assume that the bitmap is loaded with appropriate (pre-calculated) size
@Composable
internal fun RowScope.WidgetTrackInfo(
    layout: RecognitionWidgetLayout,
    track: Track,
    artwork: Bitmap?
) {
    when (layout) {
        is RecognitionWidgetLayout.Horizontal -> Row(
            modifier = GlanceModifier
                .defaultWeight()
                .fillMaxHeight()
                .padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (layout.showArtwork) {
                if (artwork != null) {
                    Image(
                        provider = ImageProvider(artwork),
                        contentDescription = null,
                        contentScale = ContentScale.Fit
                    )
                } else {
                    ArtworkPlaceholder(layout.artworkSize)
                }
                Spacer(GlanceModifier.width(artworkToTextPadding))
            }
            Column(
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.Start,
                modifier = GlanceModifier
                    .defaultWeight()
                    .fillMaxHeight()
            ) {
                Text(
                    text = track.title,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = titleTextSize,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = if (layout.isNarrow) 1 else 2
                )

                if (!layout.isNarrow) {
                    Spacer(GlanceModifier.height(subtitleTopPadding))
                    Text(
                        text = track.artist,
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            fontSize = subtitleTextSize,
                            fontWeight = FontWeight.Normal
                        ),
                        maxLines = 1
                    )
                }
            }
        }

        is RecognitionWidgetLayout.Vertical -> Column(
            modifier = GlanceModifier
                .defaultWeight()
                .fillMaxHeight()
                .padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (layout.showArtwork) {
                if (artwork != null) {
                    Image(
                        provider = ImageProvider(artwork),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                    )
                } else {
                    ArtworkPlaceholder(layout.artworkSize)
                }
                Spacer(GlanceModifier.height(artworkToTextPadding))
            }
            Column(
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = GlanceModifier.fillMaxWidth()
            ) {
                Text(
                    text = track.title,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 2
                )
                Spacer(GlanceModifier.height(subtitleTopPadding))
                Text(
                    text = track.artist,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun ArtworkPlaceholder(artworkSize: Dp) {
    Box(
        modifier = GlanceModifier
            .then(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val systemInnerRadiusDefined = LocalContext.current.resources
                        .getResourceName(android.R.dimen.system_app_widget_inner_radius) != null
                    GlanceModifier
                        .background(UiR.color.surface_container_highest)
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
