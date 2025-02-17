package com.mrsep.musicrecognizer.feature.recognition.widget.ui

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.RowScope
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.mrsep.musicrecognizer.core.domain.track.model.Track
import com.mrsep.musicrecognizer.feature.recognition.widget.ui.RecognitionWidgetLayout.Companion.artworkToTextPadding
import com.mrsep.musicrecognizer.feature.recognition.widget.ui.RecognitionWidgetLayout.Companion.contentPadding
import com.mrsep.musicrecognizer.feature.recognition.widget.ui.RecognitionWidgetLayout.Companion.subtitleTextSize
import com.mrsep.musicrecognizer.feature.recognition.widget.ui.RecognitionWidgetLayout.Companion.subtitleTopPadding
import com.mrsep.musicrecognizer.feature.recognition.widget.ui.RecognitionWidgetLayout.Companion.titleTextSize

// Assume that the bitmap is loaded with appropriate (pre-calculated) size
@Composable
internal fun RowScope.TrackInfoHorizontal(
    track: Track,
    artwork: Bitmap?,
    layout: RecognitionWidgetLayout.Horizontal,
) {
    Row(
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
                )
            } else {
                ArtworkRoundedPlaceholder(layout.artworkSize)
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
}

// Assume that the bitmap is loaded with appropriate (pre-calculated) size
@Composable
internal fun RowScope.TrackInfoVertical(
    track: Track,
    artwork: Bitmap?,
    layout: RecognitionWidgetLayout.Vertical,
) {
    Column(
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
                )
            } else {
                ArtworkRoundedPlaceholder(layout.artworkSize)
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
