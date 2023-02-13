package com.mrsep.musicrecognizer.presentation.screens.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun RecentlyScreen(
    modifier: Modifier = Modifier,
    viewModel: RecentlyViewModel = hiltViewModel(),
    onTrackClick: (mbId: String) -> Unit
) {
    val trackList by viewModel.recentTracksFlow.collectAsStateWithLifecycle()
    Column(
        modifier = modifier.fillMaxSize().padding(top = 16.dp)
    ) {
        Text(
            text = "Recents",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)
        )
        RecentlyList(
            recentTrackList = trackList,
            onTrackClick = onTrackClick
        )
    }

}