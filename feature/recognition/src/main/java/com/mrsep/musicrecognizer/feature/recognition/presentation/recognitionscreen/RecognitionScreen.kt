package com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen

import android.Manifest
import android.app.Activity
import android.app.Activity.MEDIA_PROJECTION_SERVICE
import android.media.projection.MediaProjectionManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.mrsep.musicrecognizer.core.domain.preferences.AudioCaptureMode
import com.mrsep.musicrecognizer.core.domain.preferences.UserPreferences
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionResult
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionStatus
import com.mrsep.musicrecognizer.core.domain.recognition.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.core.ui.components.RecognitionPermissionsBlockedDialog
import com.mrsep.musicrecognizer.core.ui.components.RecognitionPermissionsRationaleDialog
import com.mrsep.musicrecognizer.core.ui.findActivity
import com.mrsep.musicrecognizer.core.ui.shouldShowRationale
import com.mrsep.musicrecognizer.feature.recognition.BuildConfig
import com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen.shields.ApiUsageLimitedShield
import com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen.shields.AuthErrorShield
import com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen.shields.BadConnectionShield
import com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen.shields.FatalErrorShield
import com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen.shields.NoMatchesShield
import com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen.shields.ScheduledOfflineShield
import com.mrsep.musicrecognizer.feature.recognition.service.AudioCaptureServiceMode
import com.mrsep.musicrecognizer.feature.recognition.service.createScreenCaptureIntentForDisplay
import com.mrsep.musicrecognizer.feature.recognition.service.toServiceMode
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
    val soundLevelState = viewModel.soundLevelFlow.collectAsStateWithLifecycle(initialValue = 0f)
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val defaultCaptureMode = preferences?.defaultAudioCaptureMode
    val longClickCaptureMode = preferences?.mainButtonLongPressAudioCaptureMode

    val mediaProjectionManager = remember {
        context.getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }
    val lastRequestedAudioCaptureMode = rememberSaveable {
        mutableStateOf(AudioCaptureMode.Microphone)
    }
    val mediaProjectionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK || result.data == null) {
            if (autostart) onResetAutostart()
            return@rememberLauncherForActivityResult
        }
        result.data?.let { mediaProjectionData ->
            val captureMode = lastRequestedAudioCaptureMode.value.toServiceMode(mediaProjectionData)
            viewModel.launchRecognition(captureMode)
        }
    }

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
            when (val lastMode = lastRequestedAudioCaptureMode.value) {
                AudioCaptureMode.Microphone -> {
                    viewModel.launchRecognition(lastMode.toServiceMode(null))
                }
                AudioCaptureMode.Device,
                AudioCaptureMode.Auto -> {
                    if (preferences?.useAltDeviceSoundSource == true) {
                        viewModel.launchRecognition(lastMode.toServiceMode(null))
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val intent = mediaProjectionManager.createScreenCaptureIntentForDisplay()
                        mediaProjectionLauncher.launch(intent)
                    }
                }
            }
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

    fun checkPermissionsAndLaunchRecognition(captureMode: AudioCaptureMode) {
        lastRequestedAudioCaptureMode.value = captureMode
        if (requiredPermissionsState.allPermissionsGranted) {
            when (captureMode) {
                AudioCaptureMode.Microphone -> {
                    viewModel.launchRecognition(AudioCaptureServiceMode.Microphone)
                }
                AudioCaptureMode.Device,
                AudioCaptureMode.Auto -> {
                    if (preferences?.useAltDeviceSoundSource == true) {
                        viewModel.launchRecognition(captureMode.toServiceMode(null))
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val intent = mediaProjectionManager.createScreenCaptureIntentForDisplay()
                        mediaProjectionLauncher.launch(intent)
                    }
                }
            }
        } else if (requiredPermissionsState.shouldShowRationale) {
            showPermissionsRationaleDialog = true
        } else {
            requiredPermissionsState.launchMultiplePermissionRequest()
        }
    }

    LaunchedEffect(autostart, recognizeStatus, defaultCaptureMode) {
        if (!autostart || defaultCaptureMode == null) return@LaunchedEffect
        when (recognizeStatus) {
            RecognitionStatus.Ready -> checkPermissionsAndLaunchRecognition(defaultCaptureMode)
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
                fun onClick(captureMode: AudioCaptureMode) {
                    if (recognizeStatus.isDone()) return
                    if (preferences.vibrateOnTap()) viewModel.vibrateOnTap()
                    if (recognizeStatus is RecognitionStatus.Recognizing) {
                        viewModel.cancelRecognition()
                    } else {
                        checkPermissionsAndLaunchRecognition(captureMode)
                    }
                }
                RecognitionButtonWithTitle(
                    title = getButtonTitle(recognizeStatus, autostart),
                    onButtonClick = { defaultCaptureMode?.run(::onClick) },
                    onButtonLongClick = { longClickCaptureMode?.run(::onClick) },
                    activated = recognizeStatus is RecognitionStatus.Recognizing,
                    soundLevelState = soundLevelState,
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
                is RecognitionStatus.Done -> when (val result = thisStatus.result) {
                    is RecognitionResult.Error -> when (val remoteError = result.remoteError) {
                        RemoteRecognitionResult.Error.BadConnection -> BadConnectionShield(
                            recognitionTask = result.recognitionTask,
                            onDismissClick = viewModel::resetRecognitionResult,
                            onRetryClick = { checkPermissionsAndLaunchRecognition(lastRequestedAudioCaptureMode.value) },
                            onNavigateToQueue = { recognitionId ->
                                viewModel.resetRecognitionResult()
                                onNavigateToQueueScreen(recognitionId)
                            }
                        )

                        is RemoteRecognitionResult.Error.BadRecording -> FatalErrorShield(
                            title = stringResource(StringsR.string.result_title_recording_error),
                            message = stringResource(StringsR.string.result_message_recording_error),
                            moreInfo = remoteError.getErrorInfo(),
                            recognitionTask = result.recognitionTask,
                            onDismissClick = viewModel::resetRecognitionResult,
                            onRetryClick = { checkPermissionsAndLaunchRecognition(lastRequestedAudioCaptureMode.value) },
                            onNavigateToQueue = { recognitionId ->
                                viewModel.resetRecognitionResult()
                                onNavigateToQueueScreen(recognitionId)
                            }
                        )

                        is RemoteRecognitionResult.Error.HttpError -> FatalErrorShield(
                            title = stringResource(StringsR.string.result_title_bad_network_response),
                            message = stringResource(StringsR.string.result_message_bad_network_response),
                            moreInfo = remoteError.getErrorInfo(),
                            recognitionTask = result.recognitionTask,
                            onDismissClick = viewModel::resetRecognitionResult,
                            onRetryClick = { checkPermissionsAndLaunchRecognition(lastRequestedAudioCaptureMode.value) },
                            onNavigateToQueue = { recognitionId ->
                                viewModel.resetRecognitionResult()
                                onNavigateToQueueScreen(recognitionId)
                            }
                        )

                        is RemoteRecognitionResult.Error.UnhandledError -> FatalErrorShield(
                            title = stringResource(StringsR.string.result_title_internal_error),
                            message = stringResource(StringsR.string.result_message_internal_error),
                            moreInfo = remoteError.getErrorInfo(),
                            recognitionTask = result.recognitionTask,
                            onDismissClick = viewModel::resetRecognitionResult,
                            onRetryClick = { checkPermissionsAndLaunchRecognition(lastRequestedAudioCaptureMode.value) },
                            onNavigateToQueue = { recognitionId ->
                                viewModel.resetRecognitionResult()
                                onNavigateToQueueScreen(recognitionId)
                            }
                        )

                        is RemoteRecognitionResult.Error.AuthError -> AuthErrorShield(
                            recognitionTask = result.recognitionTask,
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
                            recognitionTask = result.recognitionTask,
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
                        recognitionTask = result.recognitionTask,
                        onDismissClick = viewModel::resetRecognitionResult,
                        onNavigateToQueue = { recognitionId ->
                            viewModel.resetRecognitionResult()
                            onNavigateToQueueScreen(recognitionId)
                        }
                    )

                    is RecognitionResult.NoMatches -> NoMatchesShield(
                        recognitionTask = result.recognitionTask,
                        onDismissClick = viewModel::resetRecognitionResult,
                        onRetryClick = { checkPermissionsAndLaunchRecognition(lastRequestedAudioCaptureMode.value) },
                        onNavigateToQueue = { recognitionId ->
                            viewModel.resetRecognitionResult()
                            onNavigateToQueueScreen(recognitionId)
                        }
                    )

                    is RecognitionResult.Success -> {
                        LaunchedEffect(result) {
                            delay(animationDurationButton.toLong())
                            onNavigateToTrackScreen(result.track.id)
                        }
                        DisposableEffect(result) {
                            onDispose(viewModel::resetRecognitionResult)
                        }
                    }
                }

                RecognitionStatus.Ready,
                is RecognitionStatus.Recognizing -> { }
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
            stringResource(StringsR.string.main_screen_tap_to_recognize)
        }

        is RecognitionStatus.Recognizing -> if (recognitionStatus.extraTry) {
            stringResource(StringsR.string.main_screen_listening_extra_time)
        } else {
            stringResource(StringsR.string.main_screen_listening)
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
