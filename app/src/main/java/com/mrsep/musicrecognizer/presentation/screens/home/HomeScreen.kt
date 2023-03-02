package com.mrsep.musicrecognizer.presentation.screens.home

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrsep.musicrecognizer.R
import com.mrsep.musicrecognizer.domain.RecognizeStatus
import com.mrsep.musicrecognizer.domain.model.RemoteRecognizeResult


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        val context = LocalContext.current
        val recognizeStatus by viewModel.recognizeStatusFlow.collectAsStateWithLifecycle()
        val superButtonTitle = getButtonTitle(recognizeStatus)
        DeveloperSection(
            modifier = Modifier.padding(16.dp),
            onRecordClickMR = { viewModel.startRecordMR() },
            onStopClickMR = { viewModel.stopRecordMR() },
            onRecordClickAR = { showStubToast(context) },
            onStopClickAR = { showStubToast(context) },
            onPlayClickMP = { viewModel.startPlayAudio() },
            onStopClickMP = { viewModel.stopPlayAudio() },
            onRecognizeClick = { viewModel.recognize() },
            onFakeRecognizeClick = { viewModel.fakeRecognize() }
        )
        SuperButtonSection(
            title = superButtonTitle,
            onButtonClick = viewModel::recognizeTap,
            activated = isStateActive(recognizeStatus),
            modifier = Modifier.padding(horizontal = 0.dp, vertical = 16.dp)
        )
        Crossfade(targetState = recognizeStatus) { status ->
            when (status) {
                RecognizeStatus.NoMatches -> {
                    DismissibleAnimatedContent(
                        onDismissed = viewModel::resetRecognizer,
                        positionalThreshold = { 120.dp.toPx() },
                        content = {
                            NotSuccessCard(
                                title = stringResource(R.string.no_matches_found),
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    )
                }
                is RecognizeStatus.Success -> {
                    DismissibleAnimatedContent(
                        onDismissed = viewModel::resetRecognizer,
                        positionalThreshold = { 120.dp.toPx() },
                        content = {
                            TrackCard(
                                track = status.track,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    )
                }
                is RecognizeStatus.Error -> {
                    DismissibleAnimatedContent(
                        onDismissed = viewModel::resetRecognizer,
                        positionalThreshold = { 120.dp.toPx() },
                        content = {
                            NotSuccessCard(
                                title = getErrorMessage(status),
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    )
                }
                else -> {}
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DismissibleAnimatedContent(
    onDismissed: () -> Unit,
    modifier: Modifier = Modifier,
    confirmValueChange: (DismissValue) -> Boolean = { true },
    positionalThreshold: Density.(totalDistance: Float) -> Float = SwipeToDismissDefaults.FixedPositionalThreshold,
    content: @Composable RowScope.() -> Unit
) {
    val dismissState = rememberDismissState(
        initialValue = DismissValue.Default,
        confirmValueChange = confirmValueChange,
        positionalThreshold = positionalThreshold
    )
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue != DismissValue.Default) {
            onDismissed()
        }
    }
    AnimatedVisibility(visible = dismissState.currentValue == DismissValue.Default) {
        SwipeToDismiss(
            state = dismissState,
            background = { },
            dismissContent = content,
            modifier = modifier
        )
    }
}

@Composable
fun NotSuccessCard(
    title: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp,
        border = BorderStroke(
            width = 1.dp,
            color = Color.Red.copy(alpha = 0.3f)
        ),
        modifier = modifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.ic_error_filled_24),
                contentDescription = null,
                modifier = Modifier.padding(24.dp)
            )
            Text(
                text = title,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 24.dp, top = 24.dp, bottom = 24.dp),
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
private fun getButtonTitle(recognizeStatus: RecognizeStatus): String {
    return when (recognizeStatus) {
        RecognizeStatus.Listening -> stringResource(R.string.listening)
        RecognizeStatus.Recognizing -> stringResource(R.string.recognizing)
        else -> stringResource(R.string.tap_for_recognize)
    }
}

@Composable
private fun getErrorMessage(status: RecognizeStatus.Error): String {
    return when (status) {
        is RecognizeStatus.Error.RecordError -> stringResource(R.string.message_record_error)
        is RecognizeStatus.Error.RemoteError -> when (status.error) {
            RemoteRecognizeResult.Error.BadConnection -> stringResource(R.string.message_bad_connection_error)
            is RemoteRecognizeResult.Error.HttpError -> stringResource(R.string.message_http_error)
            RemoteRecognizeResult.Error.LimitReached -> stringResource(R.string.message_api_limit_error)
            is RemoteRecognizeResult.Error.UnhandledError -> stringResource(R.string.message_unhandled_error)
            RemoteRecognizeResult.Error.WrongToken -> stringResource(R.string.message_api_wrong_error)
        }
    }
}

private fun isStateActive(recognizeStatus: RecognizeStatus): Boolean {
    return when (recognizeStatus) {
        RecognizeStatus.Listening, RecognizeStatus.Recognizing -> true
        else -> false
    }
}

private fun showStubToast(context: Context) {
    Toast.makeText(context, context.getString(R.string.not_implemented), Toast.LENGTH_LONG).show()
}