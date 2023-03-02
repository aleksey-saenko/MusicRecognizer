package com.mrsep.musicrecognizer.presentation.screens.home

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mrsep.musicrecognizer.R
import com.mrsep.musicrecognizer.domain.model.Track
import com.mrsep.musicrecognizer.util.forwardingPainter

@Composable
fun TrackCard(
    track: Track,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessVeryLow))
            .fillMaxWidth()
    ) {
        var lyricsExpanded by rememberSaveable { mutableStateOf(false) }
        Column {
            Row {
                val placeholder = forwardingPainter(
                    painter = painterResource(R.drawable.baseline_album_96),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                    alpha = 0.3f
                )
                AsyncImage(
                    model = track.links.artwork,
                    placeholder = placeholder,
                    error = placeholder,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.medium)
                        .size(150.dp)
                )
                Column(modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
                ) {
                    Text(
                        text = track.title,
                        modifier = Modifier,
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 1
                    )
                    Text(
                        text = track.artist,
                        modifier = Modifier,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1
                    )
                    track.album?.let {
                        Text(
                            text = track.releaseDate?.year?.let { "${track.album} ($it)" }
                                ?: track.album,
                            modifier = Modifier,
                            style = MaterialTheme.typography.titleMedium,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (track.lyrics != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(shape = MaterialTheme.shapes.medium)
                                .background(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                )
                                .clickable { lyricsExpanded = !lyricsExpanded }
                                .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
                                .align(Alignment.End)
                        ) {
                            Text(
                                text = "LYRICS",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Image(
                                imageVector = if (lyricsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                            )
                        }

                    }
                }
            }
            if (lyricsExpanded) {
                Divider(modifier = Modifier
                    .padding(top = 0.dp, bottom = 8.dp)
                    .alpha(0.3f))
                Text(
                    text = "${track.lyrics})",
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
