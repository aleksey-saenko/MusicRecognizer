package com.mrsep.musicrecognizer.presentation.screens.home

import android.Manifest
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.mrsep.musicrecognizer.R
import com.mrsep.musicrecognizer.domain.RecognizeStatus
import com.mrsep.musicrecognizer.domain.model.RemoteRecognizeResult
import com.mrsep.musicrecognizer.presentation.screens.home.components.*
import com.mrsep.musicrecognizer.presentation.screens.onboarding.DialogForOpeningAppSettings
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToTrackScreen: (mbId: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferences by viewModel.preferencesFlow.collectAsStateWithLifecycle(initialValue = null)
    val recognizeStatus by viewModel.recognizeStatusFlow.collectAsStateWithLifecycle()
    val developerMode = preferences?.developerModeEnabled ?: false

    val ampFlow by viewModel.ampFlow.collectAsStateWithLifecycle(initialValue = 0f)

    val superButtonSectionOpacity by animateFloatAsState(
        targetValue = if (recognizeStatus.isFinalState) 0f else 1f,
        animationSpec = tween(durationMillis = 100)
    )

    // permission logic block
    var scheduledJob: Job? by remember { mutableStateOf(null) }
    var permissionDialogVisible by rememberSaveable { mutableStateOf(false) }
    val recorderPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    if (permissionDialogVisible) {
        scheduledJob?.cancel()
        DialogForOpeningAppSettings(
            onConfirmClick = { permissionDialogVisible = false },
            onDismissClick = { permissionDialogVisible = false }
        )
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        if (developerMode) {
            DeveloperSection(
                modifier = Modifier.padding(16.dp),
                onRecordClickMR = { viewModel.startRecordMR() },
                onStopClickMR = { viewModel.stopRecordMR() },
                onRecordClickAR = viewModel::startRecordAR,
                onStopClickAR = { showStubToast(context) },
                onPlayClickMP = { viewModel.startPlayAudio() },
                onStopClickMP = { viewModel.stopPlayAudio() },
                onRecognizeClick = { viewModel.recognize() },
                onFakeRecognizeClick = { viewModel.fakeRecognize() },
                onClearDatabase = { viewModel.clearDatabase() },
                onPrepopulateDatabase = { viewModel.prepopulateDatabase() }
            )
        } else {
            Spacer(Modifier)
        }
        SuperButtonSection(
            title = getButtonTitle(recognizeStatus),
            onButtonClick = {
                if (recorderPermissionState.status.isGranted) {
                    viewModel.recognizeTap()
                } else if (recorderPermissionState.status.shouldShowRationale) {
                    recorderPermissionState.launchPermissionRequest()
                    scheduledJob?.cancel()
                    scheduledJob = scope.launch {
                        snapshotFlow { recorderPermissionState.status.isGranted }
                            .filter { it }.take(1)
                            .collect { viewModel.recognizeTap() }
                    }
                } else {
                    permissionDialogVisible = true
                }
            },
            activated = recognizeStatus.isInProcessState,
            amplitudeFactor = ampFlow,
            enabled = true, //recorderPermissionState.status.isGranted
            modifier = Modifier
                .alpha(superButtonSectionOpacity)
                .padding(horizontal = 0.dp, vertical = 16.dp)
        )
        Spacer(Modifier.height(if (developerMode) 48.dp else 0.dp))
    }

    when (val status = recognizeStatus) {
        RecognizeStatus.NoMatches -> {
            NoMatchFoundDialog(
                onDismissClick = viewModel::resetStatusToReady,
                onSaveClick = { showStubToast(context) }, //FIXME
                onRetryClick = viewModel::recognizeTap
            )
        }
        is RecognizeStatus.Error.RemoteError -> {
            when (status.error) {
                RemoteRecognizeResult.Error.BadConnection -> {
                    BadConnectionDialog(
                        onDismissClick = viewModel::resetStatusToReady,
                        onNavigateToQueue = { showStubToast(context) } //FIXME
                    )
                }
                is RemoteRecognizeResult.Error.WrongToken -> {
                    WrongTokenDialog(
                        isLimitReached = status.error.isLimitReached,
                        onDismissClick = viewModel::resetStatusToReady,
                        onNavigateToPreferences = { showStubToast(context) } //FIXME
                    )
                }
                is RemoteRecognizeResult.Error.HttpError -> {
                    HttpErrorDialog(
                        code = status.error.code,
                        message = status.error.message,
                        onDismissClick = viewModel::resetStatusToReady,
                        onSendReportClick = { showStubToast(context) } //FIXME
                    )
                }
                is RemoteRecognizeResult.Error.UnhandledError -> {
                    UnhandledErrorDialog(
                        throwable = status.error.e,
                        onDismissClick = viewModel::resetStatusToReady,
                        onSendReportClick = { showStubToast(context) } //FIXME
                    )
                }
            }
        }
        is RecognizeStatus.Error.RecordError -> {
            RecordErrorDialog(
                onDismissClick = viewModel::resetStatusToReady,
                onNavigateToAppInfo = { showStubToast(context) } //FIXME
            )
        }
        RecognizeStatus.Ready,
        RecognizeStatus.Listening,
        RecognizeStatus.Recognizing -> { /* no dialog & actions */
        }

        is RecognizeStatus.Success -> {
            DisposableEffect(status) {
                onNavigateToTrackScreen(status.track.mbId)
                onDispose {
                    viewModel.resetStatusToReady()
                }
            }
        }
    }
}

@Composable
private fun getButtonTitle(recognizeStatus: RecognizeStatus): String {
    return when (recognizeStatus) {
        RecognizeStatus.Ready -> stringResource(R.string.tap_for_recognize)
        RecognizeStatus.Listening -> stringResource(R.string.listening)
        RecognizeStatus.Recognizing -> stringResource(R.string.recognizing)
        else -> stringResource(R.string.tap_for_recognize)
    }
}

private fun showStubToast(context: Context) {
    Toast.makeText(context, context.getString(R.string.not_implemented), Toast.LENGTH_LONG).show()
}