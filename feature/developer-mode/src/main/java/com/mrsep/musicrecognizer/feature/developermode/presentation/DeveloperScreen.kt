package com.mrsep.musicrecognizer.feature.developermode.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DeveloperScreen(
    onBackPressed: () -> Unit,
    viewModel: DeveloperViewModel = hiltViewModel()
) {
    val topBarBehaviour = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        DeveloperScreenTopBar(
            scrollBehavior = topBarBehaviour,
            onBackPressed = onBackPressed
        )
        AnimatedVisibility(isProcessing) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )
        }
        Column(
            modifier = Modifier
                .nestedScroll(topBarBehaviour.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ButtonGroup(
                title = "DATABASE",
                content = {
                    Button(onClick = viewModel::clearDb) {
                        Text(text = "Clear")
                    }
                    Button(onClick = viewModel::prepopulateDbFakes) {
                        Text(text = "Load fake")
                    }
                    Button(onClick = viewModel::prepopulateDbAssets) {
                        Text(text = "Load real")
                    }
                    Button(onClick = viewModel::prepopulateRecognitionDb) {
                        Text(text = "Load enqueued")
                    }
                }
            )
            val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
            val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
            val amplitude by viewModel.amplitudeFlow.collectAsStateWithLifecycle(0f)
            ButtonGroup(
                title = "AudioRecorder (with encoder)",
                subtitle = "soundLevel = $amplitude",
                content = {
                    Button(
                        onClick = {
                            if (isRecording) {
                                viewModel.stopAudioRecord()
                            } else {
                                viewModel.startAudioRecord()
                            }
                        },
                        enabled = !isPlaying
                    ) {
                        Text(text = if (isRecording) "Stop rec" else "Start rec")
                    }
                    Button(
                        onClick = {
                            if (isPlaying) {
                                viewModel.stopPlayer()
                            } else {
                                viewModel.startPlayer()
                            }
                        },
                        enabled = !isRecording
                    ) {
                        Text(text = if (isPlaying) "Stop player" else "Start player")
                    }
                }
            )
        }
    }

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ButtonGroup(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.large,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            subtitle?.let {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            FlowRow(
                verticalArrangement = Arrangement.Center,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                content = content
            )
        }
    }

}