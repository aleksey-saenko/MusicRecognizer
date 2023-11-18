package com.mrsep.musicrecognizer.feature.track.presentation.track

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun TrackInfoColumn(
    title: String,
    artist: String,
    albumYear: String?,
    isExpandedScreen: Boolean,
    onCopyToClipboard: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val isCompact = if (isExpandedScreen) maxWidth < 500.dp else maxHeight < 500.dp
        val spacerWidth = if (isCompact) 6.dp else 8.dp
        val titleTextStyle = if (isCompact) {
            MaterialTheme.typography.titleLarge
        } else {
            MaterialTheme.typography.headlineMedium
        }
        val artistTextStyle = if (isCompact) {
            MaterialTheme.typography.bodyLarge
        } else {
            MaterialTheme.typography.titleLarge
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(spacerWidth),
            modifier = Modifier
        ) {
            val titleScrollState = rememberScrollState()
            val metadataToCopy = "$title - $artist"
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = titleTextStyle,
                modifier = Modifier
                    .combinedClickable(
                        onClick = { },
                        onLongClick = { onCopyToClipboard(metadataToCopy) },
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    )
                    .rowFadingEdge(
                        startEdgeInitialColor = MaterialTheme.colorScheme.background,
                        isVisibleStartEdge = titleScrollState.canScrollBackward,
                        isVisibleEndEdge = titleScrollState.canScrollForward,
                    )
                    .horizontalScroll(titleScrollState)
                    .padding(horizontal = 16.dp)
            )
            val artistScrollState = rememberScrollState()
            Text(
                text = artist,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = artistTextStyle,
                modifier = Modifier
                    .combinedClickable(
                        onClick = { },
                        onLongClick = { onCopyToClipboard(metadataToCopy) },
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    )
                    .rowFadingEdge(
                        startEdgeInitialColor = MaterialTheme.colorScheme.background,
                        isVisibleStartEdge = artistScrollState.canScrollBackward,
                        isVisibleEndEdge = artistScrollState.canScrollForward,
                    )
                    .horizontalScroll(artistScrollState)
                    .padding(horizontal = 16.dp)
            )
            val albumScrollState = rememberScrollState()
            albumYear?.let {
                Text(
                    text = albumYear,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = artistTextStyle,
                    modifier = Modifier
                        .combinedClickable(
                            onClick = { },
                            onLongClick = { onCopyToClipboard(metadataToCopy) },
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        )
                        .rowFadingEdge(
                            startEdgeInitialColor = MaterialTheme.colorScheme.background,
                            isVisibleStartEdge = albumScrollState.canScrollBackward,
                            isVisibleEndEdge = albumScrollState.canScrollForward,
                        )
                        .horizontalScroll(albumScrollState)
                        .padding(horizontal = 16.dp)
                )
            }
        }
    }
}