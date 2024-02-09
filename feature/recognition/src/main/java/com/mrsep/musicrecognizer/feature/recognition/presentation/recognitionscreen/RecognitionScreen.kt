package com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.mrsep.musicrecognizer.core.ui.components.RecorderPermissionBlockedDialog
import com.mrsep.musicrecognizer.core.ui.components.RecorderPermissionRationaleDialog
import com.mrsep.musicrecognizer.core.ui.findActivity
import com.mrsep.musicrecognizer.core.ui.shouldShowRationale
import com.mrsep.musicrecognizer.feature.recognition.BuildConfig
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionStatus
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.UserPreferences
import com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen.shields.ApiUsageLimitedShield
import com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen.shields.BadConnectionShield
import com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen.shields.FatalErrorShield
import com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen.shields.NoMatchesShield
import com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen.shields.ScheduledOfflineShield
import com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen.shields.AuthErrorShield
import kotlinx.coroutines.delay
import com.mrsep.musicrecognizer.core.strings.R as StringsR

internal const val animationDurationButton = 600
internal const val animationDurationShield = 220

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun RecognitionScreen(
    viewModel: RecognitionViewModel = hiltViewModel(),
    autostart: Boolean,
    onResetAutostart: () -> Unit,
    onNavigateToTrackScreen: (trackId: String) -> Unit,
    onNavigateToQueueScreen: (recognitionId: Int?) -> Unit,
    onNavigateToPreferencesScreen: () -> Unit
) {
    val context = LocalContext.current
    val recognizeStatus by viewModel.recognitionState.collectAsStateWithLifecycle()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val ampFlow by viewModel.maxAmplitudeFlow.collectAsStateWithLifecycle(initialValue = 0f)
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()

    //region <permission handling block>
    var permissionBlockedDialogVisible by rememberSaveable { mutableStateOf(false) }
    var permissionRationaleDialogVisible by rememberSaveable { mutableStateOf(false) }
    val recorderPermissionState = rememberPermissionState(
        Manifest.permission.RECORD_AUDIO
    ) { granted ->
        if (granted) {
            viewModel.launchRecognition()
        } else if (!context.findActivity().shouldShowRationale(Manifest.permission.RECORD_AUDIO)) {
            permissionBlockedDialogVisible = true
        }
    }
    if (permissionBlockedDialogVisible) RecorderPermissionBlockedDialog(
        onConfirmClick = { permissionBlockedDialogVisible = false },
        onDismissClick = { permissionBlockedDialogVisible = false }
    )
    if (permissionRationaleDialogVisible) RecorderPermissionRationaleDialog(
        onConfirmClick = {
            permissionRationaleDialogVisible = false
            recorderPermissionState.launchPermissionRequest()
        },
        onDismissClick = { permissionRationaleDialogVisible = false }
    )
    //endregion

    fun launchRecognition() {
        if (recorderPermissionState.status.isGranted) {
            viewModel.launchRecognition()
        } else if (recorderPermissionState.status.shouldShowRationale) {
            permissionRationaleDialogVisible = true
        } else {
            recorderPermissionState.launchPermissionRequest()
        }
    }

    LaunchedEffect(autostart) {
        if (autostart) {
            launchRecognition()
            onResetAutostart()
        }
    }

    Box(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .clip(RectangleShape)
            .statusBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        if (BuildConfig.DEBUG) {
            DebugBuildLabel(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            )
        }
        AnimatedVisibility(
            visible = recognizeStatus.isNotDone(),
            enter = enterTransitionButton,
            exit = exitTransitionButton,
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                SuperButtonSection(
                    title = getButtonTitle(recognizeStatus, autostart),
                    onButtonClick = {
                        if (preferences.vibrateOnTap()) viewModel.vibrateOnTap()
                        if (recognizeStatus is RecognitionStatus.Recognizing) {
                            viewModel.cancelRecognition()
                        } else {
                            launchRecognition()
                        }
                    },
                    activated = recognizeStatus is RecognitionStatus.Recognizing,
                    amplitudeFactor = ampFlow,
                    modifier = Modifier
                        .weight(1f)
                        .padding(
                            horizontal = 24.dp,
                            vertical = 24.dp
                        )
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
            transitionSpec = transitionSpecShield,
            label = "shieldTransition"
        ) { thisStatus ->
            when (thisStatus) {
                is RecognitionStatus.Done -> when (thisStatus.result) {
                    is RecognitionResult.Error -> when (thisStatus.result.remoteError) {
                        RemoteRecognitionResult.Error.BadConnection -> BadConnectionShield(
                            recognitionTask = thisStatus.result.recognitionTask,
                            onDismissClick = viewModel::resetRecognitionResult,
                            onRetryClick = ::launchRecognition,
                            onNavigateToQueue = { recognitionId ->
                                viewModel.resetRecognitionResult()
                                onNavigateToQueueScreen(recognitionId)
                            }
                        )

                        is RemoteRecognitionResult.Error.BadRecording -> FatalErrorShield(
                            title = stringResource(StringsR.string.recording_error),
                            message = stringResource(StringsR.string.message_record_error),
                            moreInfo = thisStatus.result.remoteError.getErrorInfo(),
                            recognitionTask = thisStatus.result.recognitionTask,
                            onDismissClick = viewModel::resetRecognitionResult,
                            onRetryClick = ::launchRecognition,
                            onNavigateToQueue = { recognitionId ->
                                viewModel.resetRecognitionResult()
                                onNavigateToQueueScreen(recognitionId)
                            }
                        )

                        is RemoteRecognitionResult.Error.HttpError -> FatalErrorShield(
                            title = stringResource(StringsR.string.bad_network_response),
                            message = stringResource(StringsR.string.message_http_error),
                            moreInfo = thisStatus.result.remoteError.getErrorInfo(),
                            recognitionTask = thisStatus.result.recognitionTask,
                            onDismissClick = viewModel::resetRecognitionResult,
                            onRetryClick = ::launchRecognition,
                            onNavigateToQueue = { recognitionId ->
                                viewModel.resetRecognitionResult()
                                onNavigateToQueueScreen(recognitionId)
                            }
                        )

                        is RemoteRecognitionResult.Error.UnhandledError -> FatalErrorShield(
                            title = stringResource(StringsR.string.internal_error),
                            message = stringResource(StringsR.string.message_unhandled_error),
                            moreInfo = thisStatus.result.remoteError.getErrorInfo(),
                            recognitionTask = thisStatus.result.recognitionTask,
                            onDismissClick = viewModel::resetRecognitionResult,
                            onRetryClick = ::launchRecognition,
                            onNavigateToQueue = { recognitionId ->
                                viewModel.resetRecognitionResult()
                                onNavigateToQueueScreen(recognitionId)
                            }
                        )

                        is RemoteRecognitionResult.Error.AuthError -> AuthErrorShield(
                            recognitionTask = thisStatus.result.recognitionTask,
                            onDismissClick = viewModel::resetRecognitionResult,
                            onNavigateToQueue = { recognitionId ->
                                viewModel.resetRecognitionResult()
                                onNavigateToQueueScreen(recognitionId)
                            },
                            onNavigateToPreferences = {
                                viewModel.resetRecognitionResult()
                                onNavigateToPreferencesScreen()
                            }
                        )

                        is RemoteRecognitionResult.Error.ApiUsageLimited -> ApiUsageLimitedShield(
                            recognitionTask = thisStatus.result.recognitionTask,
                            onDismissClick = viewModel::resetRecognitionResult,
                            onNavigateToQueue = { recognitionId ->
                                viewModel.resetRecognitionResult()
                                onNavigateToQueueScreen(recognitionId)
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
                        onNavigateToQueue = { recognitionId ->
                            viewModel.resetRecognitionResult()
                            onNavigateToQueueScreen(recognitionId)
                        }
                    )

                    is RecognitionResult.NoMatches -> NoMatchesShield(
                        recognitionTask = thisStatus.result.recognitionTask,
                        onDismissClick = viewModel::resetRecognitionResult,
                        onRetryClick = ::launchRecognition,
                        onNavigateToQueue = { recognitionId ->
                            viewModel.resetRecognitionResult()
                            onNavigateToQueueScreen(recognitionId)
                        }
                    )

                    is RecognitionResult.Success -> {
                        LaunchedEffect(thisStatus.result) {
                            delay(animationDurationButton.toLong())
                            onNavigateToTrackScreen(thisStatus.result.track.id)
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

        // optional vibration effect
        if (preferences.vibrateOnResult()) {
            LaunchedEffect(recognizeStatus) {
                val status = recognizeStatus
                if (status !is RecognitionStatus.Done) return@LaunchedEffect
                when (status.result) {
                    is RecognitionResult.ScheduledOffline,
                    is RecognitionResult.Success -> viewModel.vibrateResult(true)

                    is RecognitionResult.Error,
                    is RecognitionResult.NoMatches -> viewModel.vibrateResult(false)
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

private val transitionSpecShield:
        AnimatedContentTransitionScope<RecognitionStatus>.() -> ContentTransform = {
    fadeIn(
        animationSpec = tween(
            durationMillis = animationDurationShield,
            delayMillis = animationDurationButton
        )
    ).togetherWith(
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
private fun getButtonTitle(recognitionStatus: RecognitionStatus, skipReady: Boolean): String {
    return when (recognitionStatus) {
        RecognitionStatus.Ready -> if (skipReady) {
            " "
        } else {
            stringResource(StringsR.string.tap_to_recognize)
        }
        is RecognitionStatus.Recognizing -> if (recognitionStatus.extraTry) {
            stringResource(StringsR.string.trying_one_more_time)
        } else {
            stringResource(StringsR.string.listening)
        }

        is RecognitionStatus.Done -> " "

    }
}

@Stable
private fun UserPreferences?.vibrateOnTap() = this?.hapticFeedback?.vibrateOnTap == true

@Stable
private fun UserPreferences?.vibrateOnResult() = this?.hapticFeedback?.vibrateOnResult == true

private fun RemoteRecognitionResult.Error.getErrorInfo() = when (this) {
    RemoteRecognitionResult.Error.BadConnection,
    is RemoteRecognitionResult.Error.AuthError,
    is RemoteRecognitionResult.Error.ApiUsageLimited -> ""
    is RemoteRecognitionResult.Error.HttpError -> "Code:\n$code\n\nMessage:\n$message"
    is RemoteRecognitionResult.Error.BadRecording ->
        "Message:\n$message\n\nCause:\n${cause?.stackTraceToString()}"
    is RemoteRecognitionResult.Error.UnhandledError ->
        "Message:\n$message\n\nCause:\n${cause?.stackTraceToString()}"

}