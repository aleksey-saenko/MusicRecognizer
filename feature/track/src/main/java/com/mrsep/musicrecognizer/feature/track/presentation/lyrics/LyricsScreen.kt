package com.mrsep.musicrecognizer.feature.track.presentation.lyrics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrsep.musicrecognizer.core.ui.components.EmptyStaticTopBar
import com.mrsep.musicrecognizer.core.ui.components.LoadingStub
import com.mrsep.musicrecognizer.feature.track.presentation.track.TrackNotFoundMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LyricsScreen(
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LyricsViewModel = hiltViewModel()
) {
    val topBarBehaviour = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val uiStateInFlow by viewModel.uiStateStream.collectAsStateWithLifecycle()

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(topBarBehaviour.nestedScrollConnection)
            .verticalScroll(rememberScrollState())
    ) {
        when (val uiState = uiStateInFlow) {

            LyricsUiState.Loading -> Column {
                EmptyStaticTopBar(onBackPressed = onBackPressed)
                LoadingStub(
                    modifier = Modifier.fillMaxSize()
                )
            }

            LyricsUiState.LyricsNotFound -> Column {
                EmptyStaticTopBar(onBackPressed = onBackPressed)
                TrackNotFoundMessage(
                    modifier = Modifier.fillMaxSize()
                )
            }

            is LyricsUiState.Success -> Column {
                LyricsScreenTopBar(
                    onBackPressed = onBackPressed,
                    topAppBarScrollBehavior = topBarBehaviour
                )
                Text(
                    text = uiState.lyrics,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                )
            }
        }
    }
}