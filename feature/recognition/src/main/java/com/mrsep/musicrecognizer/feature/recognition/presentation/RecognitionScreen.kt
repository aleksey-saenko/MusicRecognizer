package com.mrsep.musicrecognizer.feature.recognition.presentation

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
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionStatus
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.presentation.components.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun RecognitionScreen(
    modifier: Modifier = Modifier,
    viewModel: RecognitionViewModel = hiltViewModel(),
    onNavigateToTrackScreen: (mbId: String) -> Unit,
    onNavigateToQueueScreen: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val recognizeStatus by viewModel.recognitionState.collectAsStateWithLifecycle()
    val ampFlow by viewModel.maxAmplitudeFlow.collectAsStateWithLifecycle(initialValue = 0f)

    LaunchedEffect(key1 = ampFlow, block = { println(ampFlow) })

    val superButtonSectionOpacity by animateFloatAsState(
        targetValue = if (recognizeStatus is RecognitionStatus.Done) 0f else 1f,
        animationSpec = tween(durationMillis = 100)
    )

    // permission logic block
    var firstAsked by rememberSaveable { mutableStateOf(true) }
    var scheduledJob: Job? by remember { mutableStateOf(null) }
    var permissionDialogVisible by rememberSaveable { mutableStateOf(false) }
    val recorderPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    val canShowPermissionRequest = recorderPermissionState.status.shouldShowRationale || firstAsked
    val isPermissionNotBlocked =
        recorderPermissionState.status.isGranted || canShowPermissionRequest
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
        verticalArrangement = Arrangement.Center
    ) {
        SuperButtonSection(
            title = getButtonTitle(recognizeStatus),
            onButtonClick = { //TODO() add dialog for forever blocked
                if (recorderPermissionState.status.isGranted) {
                    viewModel.recognizeTap()
                } else if (canShowPermissionRequest) {
                    firstAsked = false
                    recorderPermissionState.launchPermissionRequest()
                    scheduledJob?.cancel()
                    scheduledJob = snapshotFlow { recorderPermissionState.status.isGranted }
                        .filter { it }.take(1)
                        .onEach { viewModel.recognizeTap() }
                        .launchIn(scope)
                } else {
                    permissionDialogVisible = true
                }
            },
            activated = recognizeStatus is RecognitionStatus.Recognizing,
            amplitudeFactor = ampFlow,
            enabled = true, //isPermissionNotBlocked TODO() can conflict with the dialog for forever blocked
            modifier = Modifier
                .alpha(superButtonSectionOpacity)
                .padding(horizontal = 0.dp, vertical = 16.dp)
        )
    }
    val status = recognizeStatus
    if (status is RecognitionStatus.Done) {
        when (status.result) {
            is RecognitionResult.NoMatches -> NoMatchFoundDialog(
                onDismissClick = { viewModel.resetRecognitionResult() },
                onSaveClick = {
                    viewModel.enqueueRecognitionAndResetResult()
                    Toast.makeText(context, "Added to recognition queue", Toast.LENGTH_SHORT)
                        .show() //FIXME
                },
                onRetryClick = viewModel::recognizeTap
            )

            is RecognitionResult.Error -> {
                when (status.result.remoteError) {
                    RemoteRecognitionResult.Error.BadConnection -> BadConnectionDialog(
                        onDismissClick = {
                            viewModel.enqueueRecognitionAndResetResult()
                        },
                        onNavigateToQueue = {
                            viewModel.enqueueRecognitionAndResetResult()
                            onNavigateToQueueScreen()
                        }
                    )

                    RemoteRecognitionResult.Error.BadRecording -> RecordErrorDialog(
                        onDismissClick = { viewModel.resetRecognitionResult() },
                        onNavigateToAppInfo = { showStubToast(context) } //FIXME unnecessary because the permission was checked before (replace with report button)
                    )

                    is RemoteRecognitionResult.Error.HttpError -> HttpErrorDialog(
                        code = status.result.remoteError.code,
                        message = status.result.remoteError.message,
                        onDismissClick = { viewModel.resetRecognitionResult() },
                        onSendReportClick = { showStubToast(context) } //FIXME
                    )

                    is RemoteRecognitionResult.Error.UnhandledError -> UnhandledErrorDialog(
                        throwable = status.result.remoteError.t,
                        onDismissClick = { viewModel.resetRecognitionResult() },
                        onSendReportClick = { showStubToast(context) } //FIXME
                    )

                    is RemoteRecognitionResult.Error.WrongToken -> WrongTokenDialog(
                        isLimitReached = status.result.remoteError.isLimitReached,
                        onDismissClick = { viewModel.resetRecognitionResult() },
                        onNavigateToPreferences = { showStubToast(context) } //FIXME
                    )
                }
            }

            is RecognitionResult.Success -> {
                DisposableEffect(status) {
                    onNavigateToTrackScreen(status.result.track.mbId)
                    onDispose {
                        viewModel.resetRecognitionResult()
                    }
                }
            }
        }
    }
}

@Composable
private fun getButtonTitle(recognitionStatus: RecognitionStatus): String {
    return when (recognitionStatus) {
        is RecognitionStatus.Recognizing -> stringResource(StringsR.string.listening)
        else -> stringResource(StringsR.string.tap_to_recognize)
    }
}

private fun showStubToast(context: Context) {
    Toast.makeText(context, context.getString(StringsR.string.not_implemented), Toast.LENGTH_LONG).show()
}