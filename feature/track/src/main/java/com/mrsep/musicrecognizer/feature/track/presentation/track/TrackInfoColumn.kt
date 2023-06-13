package com.mrsep.musicrecognizer.feature.track.presentation.track

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
internal fun TrackInfoColumn(
    title: String,
    artist: String,
    albumYear: String?,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        if (maxHeight <= 250.dp) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
            ) {
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = artist,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge
                )
                albumYear?.let {
                    Text(
                        text = albumYear,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
            ) {
                Text(
                    text = title,
                    maxLines = if (this@BoxWithConstraints.maxHeight < 300.dp) 1 else 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = artist,
                    maxLines = if (this@BoxWithConstraints.maxHeight < 300.dp) 1 else 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleLarge
                )
                albumYear?.let {
                    Text(
                        text = albumYear,
                        maxLines = if (this@BoxWithConstraints.maxHeight < 300.dp) 1 else 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    }

}