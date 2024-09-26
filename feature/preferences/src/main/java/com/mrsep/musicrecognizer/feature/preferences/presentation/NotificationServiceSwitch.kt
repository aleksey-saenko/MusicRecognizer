package com.mrsep.musicrecognizer.feature.preferences.presentation

import android.Manifest
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.mrsep.musicrecognizer.core.ui.components.RecognitionPermissionsBlockedDialog
import com.mrsep.musicrecognizer.core.ui.components.RecognitionPermissionsRationaleDialog
import com.mrsep.musicrecognizer.core.ui.findActivity
import com.mrsep.musicrecognizer.core.ui.shouldShowRationale
import com.mrsep.musicrecognizer.feature.preferences.presentation.common.PreferenceSwitchItem
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun NotificationServiceSwitch(
    modifier: Modifier = Modifier,
    serviceEnabled: Boolean,
    setServiceEnabled: (Boolean) -> Unit
) {
    //region <permission handling block>
    val context = LocalContext.current
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
            setServiceEnabled(true)
        } else {
            val activity = context.findActivity()
            showPermissionsBlockedDialog = results
                .any { (permission, isGranted) ->
                    !isGranted && !activity.shouldShowRationale(permission)
                }
        }
    }
    if (showPermissionsRationaleDialog) {
        RecognitionPermissionsRationaleDialog(
            onConfirmClick = {
                showPermissionsRationaleDialog = false
                requiredPermissionsState.launchMultiplePermissionRequest()
            },
            onDismissClick = { showPermissionsRationaleDialog = false }
        )
    }
    if (showPermissionsBlockedDialog) {
        RecognitionPermissionsBlockedDialog(
            onConfirmClick = { showPermissionsBlockedDialog = false },
            onDismissClick = { showPermissionsBlockedDialog = false }
        )
    }
    //endregion
    PreferenceSwitchItem(
        modifier = modifier,
        title = stringResource(StringsR.string.pref_title_notification_service),
        subtitle = stringResource(StringsR.string.pref_subtitle_notification_service),
        onClick = {
            if (serviceEnabled) {
                setServiceEnabled(false)
            } else if (requiredPermissionsState.allPermissionsGranted) {
                setServiceEnabled(true)
            } else if (requiredPermissionsState.shouldShowRationale) {
                showPermissionsRationaleDialog = true
            } else {
                requiredPermissionsState.launchMultiplePermissionRequest()
            }
        },
        checked = serviceEnabled
    )
}
