package com.mrsep.musicrecognizer.feature.developermode.presentation

import android.content.Context
import android.widget.Toast
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
import com.mrsep.musicrecognizer.core.strings.R

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
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        DeveloperScreenTopBar(
            topAppBarScrollBehavior = topBarBehaviour,
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
//            val clipboardManager = LocalClipboardManager.current
//            var bufferText by remember { mutableStateOf("") }
//            LaunchedEffect(Unit) {
//                bufferText = clipboardManager.getText().toString()
//            }
//
//            val trackLinksResult by viewModel.trackLinksResult.collectAsStateWithLifecycle()
//            ButtonGroup(
//                title = "TRACK LINKS FETCHER",
//                content = {
//                    Button(onClick = { viewModel.fetchTrackLinks(bufferText) } ) {
//                        Text(text = "Fetch")
//                    }
//                    Button(onClick = { viewModel.fetchTrackLinksPure(bufferText) } ) {
//                        Text(text = "FetchPure")
//                    }
//                    Button(onClick = viewModel::resetFetchResult) {
//                        Text(text = "Reset result")
//                    }
//                }
//            )
//            Text(text = "Query URL:\n\n$bufferText")
//            Text(text = "Fetch result:\n\n$trackLinksResult")
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

@Suppress("unused")
private fun showStubToast(context: Context) {
    Toast.makeText(context, context.getString(R.string.not_implemented), Toast.LENGTH_LONG).show()
}