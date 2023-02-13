package com.mrsep.musicrecognizer.presentation.screens.track

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.mrsep.musicrecognizer.R
import com.mrsep.musicrecognizer.presentation.screens.home.PreviewDeviceNight
import com.mrsep.musicrecognizer.ui.theme.MusicRecognizerTheme
import com.mrsep.musicrecognizer.util.forwardingPainter

@Composable
fun TrackScreen(
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TrackViewModel = hiltViewModel()
) {
//    val track = fakeTrackList[0]
    val track by viewModel.trackFlow.collectAsStateWithLifecycle()
    if (track == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = modifier.fillMaxSize().padding(PaddingValues(horizontal = 16.dp)),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
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
            val placeholder = forwardingPainter(
                painter = painterResource(R.drawable.baseline_album_96),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                alpha = 0.3f
            )
            AsyncImage(
                model = track!!.links.artwork,
                placeholder = placeholder,
                error = placeholder,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .aspectRatio(1f)
                    .clip(MaterialTheme.shapes.extraLarge)

            )
            Text(
                text = track!!.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = track!!.artist,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
            )
        }
    }

}

@PreviewDeviceNight
@Composable
private fun TrackScreenPreview() {
    MusicRecognizerTheme {
        Surface {
            TrackScreen(
                onBackPressed = {}
            )
        }
    }
}