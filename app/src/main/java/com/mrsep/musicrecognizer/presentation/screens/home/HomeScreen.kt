package com.mrsep.musicrecognizer.presentation.screens.home

import android.content.res.Configuration
import android.net.Uri
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrsep.musicrecognizer.R
import com.mrsep.musicrecognizer.domain.model.RecognizeResult

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle(MainUiState.Ready)
        val superButtonTitle = when (uiState) {
            is MainUiState.Ready, is MainUiState.Failure -> stringResource(R.string.tap_for_recognize)
            is MainUiState.Listening -> stringResource(R.string.listening)
            is MainUiState.Recognizing -> stringResource(R.string.recognizing)
            is MainUiState.Success -> stringResource(R.string.tap_for_new_recognize)
        }
        DeveloperSection(
            modifier = Modifier,
            onRecordClickMR = { viewModel.startRecordMR() },
            onStopClickMR = { viewModel.stopRecordMR() },
            onRecordClickAR = { },
            onStopClickAR = { },
            onPlayClickMP = { viewModel.startPlayAudio() },
            onStopClickMP = { viewModel.stopPlayAudio() },
            onRecognizeClick = { viewModel.recognize() },
            onFakeRecognizeClick = { viewModel.fakeRecognize() }
        )
        SuperButtonSection(
            title = superButtonTitle,
            onButtonClick = viewModel::recognizeTap,
            activated = uiState is MainUiState.Listening || uiState is MainUiState.Recognizing,
            modifier = Modifier.padding(horizontal = 0.dp, vertical = 16.dp)
        )
        Crossfade(targetState = uiState) { state ->
            when (state) {
                MainUiState.Listening,
                MainUiState.Ready,
                MainUiState.Recognizing -> { /* nothing */
                }
                is MainUiState.Success -> {
                    when (val model = state.result) {
                        is RecognizeResult.InProgress -> { }
                        is RecognizeResult.Error -> { }
                        is RecognizeResult.NoMatches -> Surface(
                            shape = MaterialTheme.shapes.medium,
                            tonalElevation = 1.dp,
                            shadowElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "NoMatches",
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                        is RecognizeResult.Success -> TrackCard(
                            trackCardArgs = TrackCardArgs(
                                author = model.data.artist,
                                track = model.data.title,
                                album = model.data.album ?: "no album",
                                year = model.data.releaseDate?.year.toString(),
                                lyrics = model.data.lyrics,
                                imageUri = Uri.parse(model.data.links.artwork ?: "")
                            ),
                            modifier = Modifier.padding(top = 24.dp)
                        )
                    }
                }
                is MainUiState.Failure -> {
                    Text(
                        text = "Failure",
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

    }
}

@Preview(
    showSystemUi = true,
    device = "spec:id=reference_phone,shape=Normal,width=430,height=860,unit=dp,dpi=420",
    heightDp = 860,
    widthDp = 430,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark theme variant"
)
annotation class PreviewDeviceNight

@Preview(
    showSystemUi = true,
    device = "spec:id=reference_phone,shape=Normal,width=430,height=860,unit=dp,dpi=420",
    heightDp = 860,
    widthDp = 430,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "Dark theme variant"
)
annotation class PreviewDeviceLight