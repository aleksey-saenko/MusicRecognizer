package com.mrsep.musicrecognizer.feature.preferences.presentation

import android.Manifest
import android.content.Context
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
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.mrsep.musicrecognizer.core.ui.components.NotificationsPermissionBlockedDialog
import com.mrsep.musicrecognizer.core.ui.components.NotificationsPermissionRationaleDialog
import com.mrsep.musicrecognizer.core.ui.components.RecorderPermissionBlockedDialog
import com.mrsep.musicrecognizer.core.ui.components.RecorderPermissionRationaleDialog
import com.mrsep.musicrecognizer.core.ui.findActivity
import com.mrsep.musicrecognizer.core.ui.shouldShowRationale
import com.mrsep.musicrecognizer.feature.preferences.presentation.common.PreferenceSwitchItem
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun NotificationServiceSwitch(
    modifier: Modifier = Modifier,
    serviceEnabled: Boolean,
    toggleServiceState: (Boolean) -> Unit
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        //region <permission handling block>
        val context = LocalContext.current
        var recordBlockedDialogVisible by rememberSaveable { mutableStateOf(false) }
        var recordRationaleDialogVisible by rememberSaveable { mutableStateOf(false) }
        var notificationBlockedDialogVisible by rememberSaveable { mutableStateOf(false) }
        var notificationRationaleDialogVisible by rememberSaveable { mutableStateOf(false) }

        val notificationsPermissionState = rememberPermissionState(
            Manifest.permission.POST_NOTIFICATIONS
        ) { granted ->
            if (granted) {
                toggleServiceState(true)
            } else if (context.isPermissionBlocked(Manifest.permission.POST_NOTIFICATIONS)) {
                notificationBlockedDialogVisible = true
            }
        }
        val recordPermissionState = rememberPermissionState(
            Manifest.permission.RECORD_AUDIO
        ) { granted ->
            if (granted) {
                notificationsPermissionState.launchPermissionRequest()
            } else if (context.isPermissionBlocked(Manifest.permission.RECORD_AUDIO)) {
                recordBlockedDialogVisible = true
            }
        }

        if (recordBlockedDialogVisible) RecorderPermissionBlockedDialog(
            onConfirmClick = { recordBlockedDialogVisible = false },
            onDismissClick = { recordBlockedDialogVisible = false }
        )
        if (recordRationaleDialogVisible) RecorderPermissionRationaleDialog(
            onConfirmClick = {
                recordRationaleDialogVisible = false
                recordPermissionState.launchPermissionRequest()
            },
            onDismissClick = { recordRationaleDialogVisible = false }
        )
        if (notificationBlockedDialogVisible) NotificationsPermissionBlockedDialog(
            onConfirmClick = {
                notificationBlockedDialogVisible = false
            },
            onDismissClick = {
                notificationBlockedDialogVisible = false
            }
        )
        if (notificationRationaleDialogVisible) NotificationsPermissionRationaleDialog(
            onConfirmClick = {
                notificationRationaleDialogVisible = false
                notificationsPermissionState.launchPermissionRequest()
            },
            onDismissClick = {
                notificationRationaleDialogVisible = false
            }
        )
        //endregion
        PreferenceSwitchItem(
            title = stringResource(StringsR.string.notification_service),
            subtitle = stringResource(StringsR.string.notification_service_pref_subtitle),
            onClick = {
                if (serviceEnabled) {
                    toggleServiceState(false)
                } else {
                    if (recordPermissionState.status.isGranted &&
                        notificationsPermissionState.status.isGranted
                    ) {
                        toggleServiceState(true)
                    } else if (!recordPermissionState.status.isGranted &&
                        recordPermissionState.status.shouldShowRationale
                    ) {
                        recordRationaleDialogVisible = true
                    } else if (!notificationsPermissionState.status.isGranted &&
                        notificationsPermissionState.status.shouldShowRationale
                    ) {
                        notificationRationaleDialogVisible = true
                    } else {
                        recordPermissionState.launchPermissionRequest()
                    }
                }
            },
            checked = serviceEnabled
        )
    } else {
        PreferenceSwitchItem(
            modifier = modifier,
            title = stringResource(StringsR.string.notification_service),
            subtitle = stringResource(StringsR.string.notification_service_pref_subtitle),
            onClick = { toggleServiceState(!serviceEnabled) },
            checked = serviceEnabled
        )
    }
}

private fun Context.isPermissionBlocked(permission: String) =
    !findActivity().shouldShowRationale(permission)