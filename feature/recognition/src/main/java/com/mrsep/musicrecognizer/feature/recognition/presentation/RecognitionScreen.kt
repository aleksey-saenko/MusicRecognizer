package com.mrsep.musicrecognizer.feature.recognition.presentation

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.mrsep.musicrecognizer.core.ui.components.PermissionBlockedDialog
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionStatus
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.presentation.shields.BadConnectionShield
import com.mrsep.musicrecognizer.feature.recognition.presentation.shields.FatalErrorShield
import com.mrsep.musicrecognizer.feature.recognition.presentation.shields.NoMatchesShield
import com.mrsep.musicrecognizer.feature.recognition.presentation.shields.ScheduledOfflineShield
import com.mrsep.musicrecognizer.feature.recognition.presentation.shields.WrongTokenShield
import kotlinx.coroutines.delay
import com.mrsep.musicrecognizer.core.strings.R as StringsR

internal const val animationDurationButton = 600
internal const val animationDurationShield = 220

@OptIn(ExperimentalPermissionsApi::class, ExperimentalAnimationApi::class)
@Composable
internal fun RecognitionScreen(
    modifier: Modifier = Modifier,
    viewModel: RecognitionViewModel = hiltViewModel(),
    onNavigateToTrackScreen: (mbId: String) -> Unit,
    onNavigateToQueueScreen: (enqueuedId: Int?) -> Unit,
    onNavigateToPreferencesScreen: () -> Unit
) {
    val recognizeStatus by viewModel.recognitionState.collectAsStateWithLifecycle()
    val ampFlow by viewModel.maxAmplitudeFlow.collectAsStateWithLifecycle(initialValue = 0f)
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()

    //region <permission handling block>
    val recorderPermissionState = rememberPermissionState(
        Manifest.permission.RECORD_AUDIO
    ) { granted ->
        if (granted) viewModel.recognizeTap()
    }
    var isFirstTimeRequest by rememberSaveable { mutableStateOf(true) }
    var permissionBlockedDialogVisible by remember { mutableStateOf(false) }
    val canShowPermissionRequest =
        recorderPermissionState.status.shouldShowRationale || isFirstTimeRequest
    val isPermissionBlocked =
        !(recorderPermissionState.status.isGranted || canShowPermissionRequest)
    //endregion

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (permissionBlockedDialogVisible) PermissionBlockedDialog(
            onConfirmClick = { permissionBlockedDialogVisible = false },
            onDismissClick = { permissionBlockedDialogVisible = false }
        )

        AnimatedVisibility(
            visible = recognizeStatus.isNotDone(),
            enter = enterTransitionButton,
            exit = exitTransitionButton,
        ) {
            Column(
                modifier = modifier
                    .fillMaxSize()
//                .verticalScroll(rememberScrollState())
                ,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                SuperButtonSection(
                    title = getButtonTitle(recognizeStatus),
                    onButtonClick = {
                        if (recorderPermissionState.status.isGranted) {
                            viewModel.recognizeTap()
                        } else if (canShowPermissionRequest) {
                            recorderPermissionState.launchPermissionRequest()
                            isFirstTimeRequest = false
                        } else {
                            permissionBlockedDialogVisible = true
                        }
                    },
                    activated = recognizeStatus is RecognitionStatus.Recognizing,
                    amplitudeFactor = ampFlow,
                    permissionBlocked = isPermissionBlocked,
                    modifier = Modifier
                        .weight(1f)
                        .padding(
                            horizontal = 24.dp,
                            vertical = 24.dp
                        ) //.fillMaxSize().background(Color.Green)
                )
                OfflineModePopup(
                    visible = isOffline,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
        }

        BackHandler(
            enabled = recognizeStatus.isDone(),
            onBack = viewModel::resetRecognitionResult
        )
        AnimatedContent(
            targetState = recognizeStatus,
            contentAlignment = Alignment.Center,
            transitionSpec = transitionSpecShield
        ) { thisStatus ->
            when (thisStatus) {
                is RecognitionStatus.Done -> when (thisStatus.result) {
                    is RecognitionResult.Error -> when (thisStatus.result.remoteError) {
                        RemoteRecognitionResult.Error.BadConnection -> BadConnectionShield(
                            recognitionTask = thisStatus.result.recognitionTask,
                            onDismissClick = viewModel::resetRecognitionResult,
                            onRetryClick = viewModel::recognizeTap,
                            onNavigateToQueue = { enqueuedId ->
                                viewModel.resetRecognitionResult()
                                onNavigateToQueueScreen(enqueuedId)
                            }
                        )

                        is RemoteRecognitionResult.Error.BadRecording -> FatalErrorShield(
                            title = stringResource(StringsR.string.recording_error),
                            message = stringResource(StringsR.string.message_record_error),
                            moreInfo = thisStatus.result.remoteError.cause.stackTraceToString(),
                            recognitionTask = thisStatus.result.recognitionTask,
                            onDismissClick = viewModel::resetRecognitionResult,
                            onRetryClick = viewModel::recognizeTap,
                            onNavigateToQueue = { enqueuedId ->
                                viewModel.resetRecognitionResult()
                                onNavigateToQueueScreen(enqueuedId)
                            }
                        )

                        is RemoteRecognitionResult.Error.HttpError -> FatalErrorShield(
                            title = stringResource(StringsR.string.bad_network_response),
                            message = stringResource(StringsR.string.message_http_error),
                            moreInfo = "Code: ${thisStatus.result.remoteError.code}\n" +
                                    "Message: ${thisStatus.result.remoteError.message}",
                            recognitionTask = thisStatus.result.recognitionTask,
                            onDismissClick = viewModel::resetRecognitionResult,
                            onRetryClick = viewModel::recognizeTap,
                            onNavigateToQueue = { enqueuedId ->
                                viewModel.resetRecognitionResult()
                                onNavigateToQueueScreen(enqueuedId)
                            }
                        )

                        is RemoteRecognitionResult.Error.UnhandledError -> FatalErrorShield(
                            title = stringResource(StringsR.string.internal_error),
                            message = stringResource(StringsR.string.message_unhandled_error),
                            moreInfo = thisStatus.result.remoteError.t?.stackTraceToString()
                                ?: thisStatus.result.remoteError.message,
                            recognitionTask = thisStatus.result.recognitionTask,
                            onDismissClick = viewModel::resetRecognitionResult,
                            onRetryClick = viewModel::recognizeTap,
                            onNavigateToQueue = { enqueuedId ->
                                viewModel.resetRecognitionResult()
                                onNavigateToQueueScreen(enqueuedId)
                            }
                        )

                        is RemoteRecognitionResult.Error.WrongToken -> WrongTokenShield(
                            isLimitReached = thisStatus.result.remoteError.isLimitReached,
                            recognitionTask = thisStatus.result.recognitionTask,
                            onDismissClick = viewModel::resetRecognitionResult,
                            onNavigateToQueue = { enqueuedId ->
                                viewModel.resetRecognitionResult()
                                onNavigateToQueueScreen(enqueuedId)
                            },
                            onNavigateToPreferences = {
                                viewModel.resetRecognitionResult()
                                onNavigateToPreferencesScreen()
                            }
                        )
                    }

                    is RecognitionResult.ScheduledOffline -> ScheduledOfflineShield(
                        recognitionTask = thisStatus.result.recognitionTask,
                        onDismissClick = viewModel::resetRecognitionResult,
                        onNavigateToQueue = { enqueuedId ->
                            viewModel.resetRecognitionResult()
                            onNavigateToQueueScreen(enqueuedId)
                        }
                    )

                    is RecognitionResult.NoMatches -> NoMatchesShield(
                        recognitionTask = thisStatus.result.recognitionTask,
                        onDismissClick = viewModel::resetRecognitionResult,
                        onRetryClick = viewModel::recognizeTap,
                        onNavigateToQueue = { enqueuedId ->
                            viewModel.resetRecognitionResult()
                            onNavigateToQueueScreen(enqueuedId)
                        }
                    )

                    is RecognitionResult.Success -> {
                        LaunchedEffect(thisStatus.result) {
                            delay(animationDurationButton.toLong())
                            onNavigateToTrackScreen(thisStatus.result.track.mbId)
                        }
                        DisposableEffect(thisStatus.result) {
                            onDispose(viewModel::resetRecognitionResult)
                        }
                    }
                }

                RecognitionStatus.Ready,
                is RecognitionStatus.Recognizing -> {
                }
            }
        }


    }

}

private val enterTransitionButton = slideInVertically(
    animationSpec = tween(
        durationMillis = animationDurationButton,
        delayMillis = animationDurationShield,
        easing = EaseOutBack
    ),
    initialOffsetY = { fullHeight -> fullHeight }
).plus(
    fadeIn(
        tween(
            durationMillis = animationDurationButton,
            delayMillis = animationDurationShield
        ),
        initialAlpha = 0.8f
    )
)

private val exitTransitionButton = slideOutVertically(
    animationSpec = tween(
        durationMillis = animationDurationButton,
        delayMillis = 0,
        easing = EaseInCubic
    ),
    targetOffsetY = { fullHeight -> fullHeight }
).plus(
    fadeOut(
        tween(
            durationMillis = animationDurationButton,
            delayMillis = 0
        ),
        targetAlpha = 0.8f
    )
)

@OptIn(ExperimentalAnimationApi::class)
private val transitionSpecShield: AnimatedContentScope<RecognitionStatus>.() -> ContentTransform = {
    fadeIn(
        animationSpec = tween(
            durationMillis = animationDurationShield,
            delayMillis = animationDurationButton
        )
    ).with(
        fadeOut(
            animationSpec = tween(
                durationMillis = animationDurationShield,
                delayMillis = 0
            )
        )
    ).using(
        SizeTransform { _, _ ->
            snap(delayMillis = animationDurationShield)
        }
    )
}

private fun RecognitionStatus.isDone() = this is RecognitionStatus.Done
private fun RecognitionStatus.isNotDone() = !isDone()

@Composable
private fun getButtonTitle(recognitionStatus: RecognitionStatus): String {
    return when (recognitionStatus) {
        RecognitionStatus.Ready -> stringResource(StringsR.string.tap_to_recognize)
        is RecognitionStatus.Recognizing -> {
            if (recognitionStatus.extraTry)
                stringResource(StringsR.string.trying_one_more_time)
            else
                stringResource(StringsR.string.listening)
        }

        is RecognitionStatus.Done -> ""

    }
}