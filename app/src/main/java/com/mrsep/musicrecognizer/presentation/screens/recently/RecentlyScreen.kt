package com.mrsep.musicrecognizer.presentation.screens.recently

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrsep.musicrecognizer.R
import kotlinx.collections.immutable.toImmutableList

@Composable
fun RecentlyScreen(
    modifier: Modifier = Modifier,
    viewModel: RecentlyViewModel = hiltViewModel(),
    onTrackClick: (mbId: String) -> Unit
) {
    val resentsList by viewModel.recentTracksFlow.collectAsStateWithLifecycle()
    val favoritesList by viewModel.favoriteTracksFlow.collectAsStateWithLifecycle()
    val foundList by viewModel.foundTracksFlow.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TrackSearchBar(
            onSearch = { query -> viewModel.submitSearchKeyword(query) },
            modifier = Modifier.padding(top = 16.dp)
        )
        AnimatedVisibility(visible = foundList.isNotEmpty()) {
            Column {
                Text(
                    text = "Search result",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp)
                )
                RecentlyList(
                    recentTrackList = foundList.toImmutableList(),
                    onTrackClick = onTrackClick,
                    modifier = Modifier.padding(start = 8.dp, top = 16.dp)
                )
            }
        }
        if (resentsList.isNotEmpty()) {
            Text(
                text = "Recents",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp)
            )
            RecentlyList(
                recentTrackList = resentsList.toImmutableList(),
                onTrackClick = onTrackClick,
                modifier = Modifier.padding(start = 8.dp, top = 16.dp)
            )
        }
        if (favoritesList.isNotEmpty()) {
            Text(
                text = "Favorites",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp)
            )
            RecentlyList(
                recentTrackList = favoritesList.toImmutableList(),
                onTrackClick = onTrackClick,
                modifier = Modifier.padding(start = 8.dp, top = 16.dp)
            )
        }
        if (resentsList.isEmpty() && favoritesList.isEmpty()) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(start = 24.dp, end = 24.dp)
                    .weight(1f)
            ) {
                Text(
                    text = "Your recent recognized and favorites songs will be shown here",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
                Icon(
                    painter = painterResource(R.drawable.baseline_recently_24),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(24.dp)
                        .size(80.dp),
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
            }
        }
    }

}