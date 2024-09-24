package com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.mrsep.musicrecognizer.core.ui.components.RecognitionPermissionsBlockedDialog
import com.mrsep.musicrecognizer.core.ui.components.RecognitionPermissionsRationaleDialog
import com.mrsep.musicrecognizer.core.ui.findActivity
import com.mrsep.musicrecognizer.core.ui.shouldShowRationale
import com.mrsep.musicrecognizer.feature.recognition.BuildConfig
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionStatus
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.UserPreferences
import com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen.shields.ApiUsageLimitedShield
import com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen.shields.AuthErrorShield
import com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen.shields.BadConnectionShield
import com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen.shields.FatalErrorShield
import com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen.shields.NoMatchesShield
import com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen.shields.ScheduledOfflineShield
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
    onNavigateToPreferencesScreen: () -> Unit,
) {
    val context = LocalContext.current
    val recognizeStatus by viewModel.recognitionState.collectAsStateWithLifecycle()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val ampFlow by viewModel.maxAmplitudeFlow.collectAsStateWithLifecycle(initialValue = 0f)
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()

    //region <permission handling block>
    var showPermissionsRationaleDialog by rememberSaveable { mutableStateOf(false) }
    var showPermissionsBlockedDialog by rememberSaveable { mutableStateOf(false) }
    val requiredPermissionsState = rememberMultiplePermissionsState(
        permissions = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.RECORD_AUDIO)
        } else {
            listOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.POST_NOTIFICATIONS)
        }
    ) { results ->
        if (results.all { (_, isGranted) -> isGranted }) {
            viewModel.launchRecognition()
        } else {
            val activity = context.findActivity()
            showPermissionsBlockedDialog = results
                .any { (permission, isGranted) ->
                    !isGranted && !activity.shouldShowRationale(permission)
                }
            if (autostart) onResetAutostart()
        }
    }
    if (showPermissionsRationaleDialog) {
        RecognitionPermissionsRationaleDialog(
            onConfirmClick = {
                showPermissionsRationaleDialog = false
                requiredPermissionsState.launchMultiplePermissionRequest()
            },
            onDismissClick = {
                showPermissionsRationaleDialog = false
                if (autostart) onResetAutostart()
            }
        )
    }
    if (showPermissionsBlockedDialog) {
        RecognitionPermissionsBlockedDialog(
            onConfirmClick = { showPermissionsBlockedDialog = false },
            onDismissClick = { showPermissionsBlockedDialog = false }
        )
    }
    //endregion

    fun launchRecognition() {
        if (requiredPermissionsState.allPermissionsGranted) {
            viewModel.launchRecognition()
        } else if (requiredPermissionsState.shouldShowRationale) {
            showPermissionsRationaleDialog = true
        } else {
            requiredPermissionsState.launchMultiplePermissionRequest()
        }
    }

    LaunchedEffect(autostart, recognizeStatus) {
        if (!autostart) return@LaunchedEffect
        when (recognizeStatus) {
            RecognitionStatus.Ready -> launchRecognition()
            is RecognitionStatus.Recognizing,
            is RecognitionStatus.Done -> onResetAutostart()
        }
    }

    Box(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.surface)
            .fillMaxSize()
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
        Box {
            AnimatedVisibility(
                visible = recognizeStatus.isNotDone(),
                enter = enterTransitionButton,
                exit = exitTransitionButton,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                RecognitionButtonWithTitle(
                    title = getButtonTitle(recognizeStatus, autostart),
                    onButtonClick = {
                        if (recognizeStatus.isDone()) return@RecognitionButtonWithTitle
                        if (preferences.vibrateOnTap()) viewModel.vibrateOnTap()
                        if (recognizeStatus is RecognitionStatus.Recognizing) {
                            viewModel.cancelRecognition()
                        } else {
                            launchRecognition()
                        }
                    },
                    activated = recognizeStatus is RecognitionStatus.Recognizing,
                    amplitudeFactor = ampFlow,
                    modifier = Modifier.padding(24.dp)
                )
            }
            AnimatedVisibility(
                visible = isOffline && recognizeStatus.isNotDone(),
                enter = enterTransitionBottomPopup,
                exit = exitTransitionBottomPopup,
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                OfflineModePopup(modifier = Modifier.padding(bottom = 16.dp))
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
            val vibrated = rememberSaveable { mutableStateOf(false) }
            LaunchedEffect(recognizeStatus) {
                val status = recognizeStatus
                if (status !is RecognitionStatus.Done) {
                    vibrated.value = false
                    return@LaunchedEffect
                }
                if (vibrated.value) return@LaunchedEffect
                when (status.result) {
                    is RecognitionResult.ScheduledOffline,
                    is RecognitionResult.Success -> viewModel.vibrateResult(true)

                    is RecognitionResult.Error,
                    is RecognitionResult.NoMatches -> viewModel.vibrateResult(false)
                }
                vibrated.value = true
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

private val enterTransitionBottomPopup = fadeIn(
    animationSpec = tween(
        durationMillis = animationDurationButton / 2,
        delayMillis = animationDurationButton
    )
) + scaleIn(
    animationSpec = tween(
        durationMillis = animationDurationButton / 2,
        delayMillis = animationDurationButton
    ),
    initialScale = 0.2f
)

private val exitTransitionBottomPopup = fadeOut(
    animationSpec = tween(durationMillis = animationDurationButton / 2)
) + scaleOut(
    animationSpec = tween(durationMillis = animationDurationButton / 2),
    targetScale = 0.2f
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
