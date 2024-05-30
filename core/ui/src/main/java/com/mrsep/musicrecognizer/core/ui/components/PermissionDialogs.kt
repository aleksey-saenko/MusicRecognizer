package com.mrsep.musicrecognizer.core.ui.components

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@Composable
fun RecognitionPermissionsRationaleDialog(
    onConfirmClick: () -> Unit,
    onDismissClick: () -> Unit
) {
    PermissionRationaleDialog(
        title = stringResource(StringsR.string.permissions),
        text = buildString {
            append(stringResource(StringsR.string.recorder_permission_rationale_message))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                append(" ")
                append(stringResource(StringsR.string.notificaiton_permission_rationale_message))
            }
        },
        onConfirmClick = onConfirmClick,
        onDismissClick = onDismissClick
    )
}

@Composable
fun RecognitionPermissionsBlockedDialog(
    onConfirmClick: () -> Unit,
    onDismissClick: () -> Unit
) {
    PermissionBlockedDialog(
        title = stringResource(StringsR.string.permissions),
        text = buildString {
            append(stringResource(StringsR.string.recorder_permission_rationale_message))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                append(" ")
                append(stringResource(StringsR.string.notificaiton_permission_rationale_message))
            }
            append("\n")
            append(stringResource(StringsR.string.permissions_denied_message))
        },
        onConfirmClick = onConfirmClick,
        onDismissClick = onDismissClick
    )
}

@Composable
private fun PermissionBlockedDialog(
    title: String,
    text: String,
    onConfirmClick: () -> Unit,
    onDismissClick: () -> Unit
) {
    val context = LocalContext.current
    val appSettingsIntent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    )
    AlertDialog(
        onDismissRequest = onDismissClick,
        confirmButton = {
            if (appSettingsIntent.resolveActivity(context.packageManager) != null) {
                TextButton(onClick = {
                    onConfirmClick()
                    context.startActivity(appSettingsIntent)
                }) {
                    Text(text = stringResource(StringsR.string.open_settings))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissClick) {
                Text(text = stringResource(StringsR.string.not_now))
            }
        },
        title = {
            Text(text = title)
        },
        text = {
            Text(text = text)
        }
    )
}

@Composable
private fun PermissionRationaleDialog(
    title: String,
    text: String,
    onConfirmClick: () -> Unit,
    onDismissClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissClick,
        confirmButton = {
            TextButton(onClick = {
                onConfirmClick()
            }) {
                Text(text = stringResource(StringsR.string.request_permission))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissClick) {
                Text(text = stringResource(StringsR.string.not_now))
            }
        },
        title = {
            Text(text = title)
        },
        text = {
            Text(
                text = text
            )
        }
    )
}