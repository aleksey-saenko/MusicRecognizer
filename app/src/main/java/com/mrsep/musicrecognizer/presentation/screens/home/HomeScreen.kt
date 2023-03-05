package com.mrsep.musicrecognizer.presentation.screens.home

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrsep.musicrecognizer.R
import com.mrsep.musicrecognizer.domain.RecognizeStatus
import com.mrsep.musicrecognizer.domain.model.RemoteRecognizeResult

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
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
        val preferences by viewModel.preferencesFlow.collectAsStateWithLifecycle(initialValue = null)
        val recognizeStatus by viewModel.recognizeStatusFlow.collectAsStateWithLifecycle()
        val superButtonTitle = getButtonTitle(recognizeStatus)
        val developerMode = preferences?.developerModeEnabled ?: false
        AnimatedContent(
            targetState = developerMode,
            modifier = Modifier.animateContentSize()
        ) { devMode ->
            if (devMode) {
                DeveloperSection(
                    modifier = Modifier.padding(16.dp),
                    onRecordClickMR = { viewModel.startRecordMR() },
                    onStopClickMR = { viewModel.stopRecordMR() },
                    onRecordClickAR = { showStubToast(context) },
                    onStopClickAR = { showStubToast(context) },
                    onPlayClickMP = { viewModel.startPlayAudio() },
                    onStopClickMP = { viewModel.stopPlayAudio() },
                    onRecognizeClick = { viewModel.recognize() },
                    onFakeRecognizeClick = { viewModel.fakeRecognize() },
                    onClearDatabase = { viewModel.clearDatabase() },
                    onPrepopulateDatabase = { viewModel.prepopulateDatabase() }
                )
            } else {
                Spacer(Modifier.height(0.dp))
            }
        }
        SuperButtonSection(
            title = superButtonTitle,
            onButtonClick = viewModel::recognizeTap,
            activated = isStateActive(recognizeStatus),
            modifier = Modifier.padding(horizontal = 0.dp, vertical = 16.dp)
        )
        AnimatedContent(
            targetState = recognizeStatus,
            modifier = Modifier.animateContentSize()
        ) { status ->
            when (status) {
                RecognizeStatus.NoMatches -> {
                    val dismissState = rememberDismissState(
                        confirmValueChange = { dismissValue ->
                            if (dismissValue != DismissValue.Default) viewModel.resetRecognizer()
                            true
                        },
                        positionalThreshold = { totalDistance -> 0.3f * totalDistance }
                    )
                    SwipeToDismiss(
                        state = dismissState,
                        background = { },
                        dismissContent = {
                            NotSuccessCard(
                                title = stringResource(R.string.no_matches_found),
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    )
                }
                is RecognizeStatus.Success -> {
                    val dismissState = rememberDismissState(
                        confirmValueChange = { dismissValue ->
                            when (dismissValue) {
                                DismissValue.Default -> {}
                                DismissValue.DismissedToEnd -> {
                                    viewModel.toFavoritesAndResetRecognizer()
                                }
                                DismissValue.DismissedToStart -> {
                                    viewModel.resetRecognizer()
                                }
                            }
                            true
                        },
                        positionalThreshold = { totalDistance -> 0.3f * totalDistance }
                    )
                    SwipeToDismiss(
                        state = dismissState,
                        background = { TrackBackgroundForDismissible(dismissState) },
                        dismissContent = {
                            TrackCard(
                                track = status.track,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    )
                }
                is RecognizeStatus.Error -> {
                    val dismissState = rememberDismissState(
                        confirmValueChange = { dismissValue ->
                            if (dismissValue != DismissValue.Default) viewModel.resetRecognizer()
                            true
                        },
                        positionalThreshold = { totalDistance -> 0.3f * totalDistance }
                    )
                    SwipeToDismiss(
                        state = dismissState,
                        background = { },
                        dismissContent = {
                            NotSuccessCard(
                                title = getErrorMessage(status),
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    )
                }
                else -> {
                    Spacer(modifier = Modifier)
                }
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RowScope.TrackBackgroundForDismissible(
    dismissState: DismissState,
    modifier: Modifier = Modifier
) {
    val direction = dismissState.dismissDirection ?: return
    val actionColor by animateColorAsState(
        targetValue = when (dismissState.targetValue) {
            DismissValue.Default -> Color.Transparent
            DismissValue.DismissedToEnd -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            DismissValue.DismissedToStart -> Color.Red.copy(alpha = 0.3f)
        }
    )
    val backgroundColors = when (direction) {
        DismissDirection.StartToEnd -> listOf(
            Color.Transparent,
            actionColor
        )
        DismissDirection.EndToStart -> listOf(
            actionColor,
            Color.Transparent
        )
    }
    val alignment = when (direction) {
        DismissDirection.StartToEnd -> Alignment.CenterStart
        DismissDirection.EndToStart -> Alignment.CenterEnd
    }
    val icon = when (direction) {
        DismissDirection.StartToEnd -> Icons.Default.Favorite
        DismissDirection.EndToStart -> Icons.Default.Close
    }
    val scale by animateFloatAsState(
        if (dismissState.targetValue == DismissValue.Default) 0.75f else 1f
    )

    Box(
        modifier
            .fillMaxSize()
            .padding(vertical = 16.dp)
            .background(brush = Brush.horizontalGradient(colors = backgroundColors))
            .padding(horizontal = 24.dp),
        contentAlignment = alignment
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.scale(scale)
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