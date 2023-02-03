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
import com.mrsep.musicrecognizer.R
import com.mrsep.musicrecognizer.ui.theme.MusicRecognizerTheme
import com.mrsep.musicrecognizer.util.parseYear

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
        val uiState by viewModel.uiState.collectAsState()
        val superButtonTitle = when (uiState) {
            MainUiState.Ready -> stringResource(R.string.tap_to_recognize)
            MainUiState.Listening -> stringResource(R.string.listening)
            MainUiState.Recognizing -> stringResource(R.string.recognizing)
            is MainUiState.Success -> stringResource(R.string.tap_to_new_recognize)
        }
        DeveloperSection(
            modifier = Modifier,
            onRecordClickMR = { viewModel.startRecordMR() },
            onStopClickMR = { viewModel.stopRecordMR() },
            onRecordClickAR = { viewModel.startRecordAR() },
            onStopClickAR = { },
            onPlayClickMP = { viewModel.startPlayAudio() },
            onStopClickMP = { viewModel.stopPlayAudio() },
            onRecognizeClick = { viewModel.recognize() },
            onFakeRecognizeClick = { viewModel.fakeRecognize() }
        )
        SuperButtonSection(
            title = superButtonTitle,
            onButtonClick = { viewModel.recognizeTap() },
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
                    state.data.result?.let { model ->
                        TrackCard(
                            trackCardArgs = TrackCardArgs(
                                author = model.artist,
                                track = model.title,
                                album = model.album,
                                year = parseYear(model.releaseDate),
                                lyrics = model.lyrics?.lyrics,
                                imageUri = Uri.parse(model.deezer?.album?.coverBig)
                            ),
                            modifier = Modifier.padding(top = 24.dp)
                        )
                    } ?: Surface(
                        shape = MaterialTheme.shapes.medium,
                        tonalElevation = 1.dp,
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = state.data.toString(),
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
}

@DevicePreviewNight
@Composable
fun DefaultPreview() {
    MusicRecognizerTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            HomeScreen()
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
annotation class DevicePreviewNight

@Preview(
    showSystemUi = true,
    device = "spec:id=reference_phone,shape=Normal,width=430,height=860,unit=dp,dpi=420",
    heightDp = 860,
    widthDp = 430,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "Dark theme variant"
)
annotation class DevicePreviewLight