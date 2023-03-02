package com.mrsep.musicrecognizer.presentation.screens.recently

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.toImmutableList

@Composable
fun RecentlyScreen(
    modifier: Modifier = Modifier,
    viewModel: RecentlyViewModel = hiltViewModel(),
    onTrackClick: (mbId: String) -> Unit
) {
    val resentsList by viewModel.recentTracksFlow.collectAsStateWithLifecycle()
    val favoritesList by viewModel.favoriteTracksFlow.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {

        TrackSearchBar()
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

}