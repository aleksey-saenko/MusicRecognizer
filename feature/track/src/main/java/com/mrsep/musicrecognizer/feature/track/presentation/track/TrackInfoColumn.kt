package com.mrsep.musicrecognizer.feature.track.presentation.track

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.ui.util.copyTextToClipboard

@Composable
internal fun TrackInfoColumn(
    title: String,
    artist: String,
    album: String?,
    year: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val metadataToCopy = "$title - $artist"
    var expandedInfo by rememberSaveable { mutableStateOf(false) }
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .combinedClickable(
                interactionSource = null,
                indication = null,
                onClick = { expandedInfo = !expandedInfo },
                onLongClick = { context.copyTextToClipboard(metadataToCopy) }
            )
    ) {
        Text(
            text = title,
            maxLines = if (expandedInfo) Int.MAX_VALUE else 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.W500),
            modifier = Modifier
        )
        Text(
            text = artist,
            maxLines = if (expandedInfo) Int.MAX_VALUE else 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
        )
        val albumAndYear = year?.let { "$album - $year" } ?: album
        albumAndYear?.let {
            Text(
                text = albumAndYear,
                maxLines = if (expandedInfo) Int.MAX_VALUE else 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
            )
        }
    }
}
