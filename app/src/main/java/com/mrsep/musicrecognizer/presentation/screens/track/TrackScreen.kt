package com.mrsep.musicrecognizer.presentation.screens.track

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.mrsep.musicrecognizer.R
import com.mrsep.musicrecognizer.domain.model.Track
import com.mrsep.musicrecognizer.presentation.common.LoadingStub
import com.mrsep.musicrecognizer.util.forwardingPainter

private const val ABOUT_TRACK_SECTION = 1
private const val LYRICS_SECTION = 2

@Composable
fun TrackScreen(
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TrackViewModel = hiltViewModel()
) {
    val uiStateInFlow by viewModel.uiStateStream.collectAsStateWithLifecycle()

    when (val uiState = uiStateInFlow) {
        TrackUiState.Loading -> LoadingStub()
        TrackUiState.TrackNotFound -> {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "Track not found",
                    modifier = Modifier.padding(8.dp)
                )
                Button(
                    onClick = onBackPressed,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Back")
                }
            }
        }
        is TrackUiState.Success -> {
            var currentSection by rememberSaveable { mutableStateOf(ABOUT_TRACK_SECTION) }

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                TrackScreenTopBar(
                    currentSection = currentSection,
                    onBackPressed = onBackPressed,
                    onAboutTrackClick = { currentSection = ABOUT_TRACK_SECTION },
                    onLyricsClick = { currentSection = LYRICS_SECTION },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                when (currentSection) {
                    ABOUT_TRACK_SECTION -> {
                        AboutTrackSection(
                            track = uiState.data,
                            onFavoriteIconClick = viewModel::onFavoriteClick,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    LYRICS_SECTION -> {
                        LyricsSection(
                            lyrics = uiState.data.lyrics ?: "not found",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    else -> throw IllegalStateException("Wrong section of the track screen")
                }
            }
        }
    }

}

@Composable
private fun TrackScreenTopBar(
    currentSection: Int,
    onBackPressed: () -> Unit,
    onAboutTrackClick: () -> Unit,
    onLyricsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = null,
            modifier = Modifier
                .padding(PaddingValues(vertical = 16.dp))
                .size(32.dp)
                .clip(MaterialTheme.shapes.small)
                .clickable { onBackPressed() }
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "ABOUT TRACK",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
                .run {
                    if (currentSection == ABOUT_TRACK_SECTION) {
                        background(
                            color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                        )
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                shape = MaterialTheme.shapes.medium
                            )
                    } else {
                        clickable { onAboutTrackClick() }
                    }
                }
                .padding(8.dp)

        )
        Text(
            text = "LYRICS",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
//                .padding(end = 16.dp)
                .clip(MaterialTheme.shapes.medium)
                .run {
                    if (currentSection == LYRICS_SECTION) {
                        background(
                            color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                        )
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                shape = MaterialTheme.shapes.medium
                            )
                    } else {
                        clickable { onLyricsClick() }
                    }
                }
                .padding(8.dp)
        )
    }
}


@Composable
private fun LyricsSection(
    lyrics: String,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier
    ) {
        Text(
            text = lyrics,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
private fun AboutTrackSection(
    track: Track,
    onFavoriteIconClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
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
                .padding(bottom = 16.dp)
                .aspectRatio(1f)
                .clip(MaterialTheme.shapes.extraLarge)

        )
        Surface(
            tonalElevation = 1.dp,
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = track.title,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .weight(1f)
                    )
                    Icon(
                        imageVector = if (track.metadata.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(onClick = onFavoriteIconClick)
                            .padding(8.dp)
                    )

                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { }
                            .padding(8.dp)
                    )
                }

                Text(
                    text = track.artist,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                track.album?.let {
                    Text(
                        text = track.releaseDate?.year?.let { "${track.album} ($it)" }
                            ?: track.album,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 8.dp,
                        alignment = Alignment.End
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text("Spotify")
                        },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_spotify),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text("Youtube")
                        },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_youtube),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text("Apple Music")
                        },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_apple),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text("Deezer")
                        },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_deezer),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text("Napster")
                        },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_napster),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text("MusicBrainz")
                        },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_musicbrainz),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}