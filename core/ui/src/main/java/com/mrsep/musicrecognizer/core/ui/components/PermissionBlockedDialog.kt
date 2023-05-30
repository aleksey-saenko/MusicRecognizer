package com.mrsep.musicrecognizer.core.ui.components

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@Composable
fun PermissionBlockedDialog(
    onConfirmClick: () -> Unit,
    onDismissClick: () -> Unit
) {
    val context = LocalContext.current
    val appSettingsIntent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    )
    if (appSettingsIntent.resolveActivity(context.packageManager) == null) {
        //FIXME should be version of alert dialog without open settings button
        Toast.makeText(context, stringResource(StringsR.string.permission_denied), Toast.LENGTH_SHORT)
            .show()
    } else {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {
                Button(onClick = {
                    onConfirmClick()
                    context.startActivity(appSettingsIntent)
                }) {
                    Text(text = stringResource(StringsR.string.open_settings))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismissClick) {
                    Text(text = stringResource(StringsR.string.back))
                }
            },
            title = {
                Text(text = stringResource(StringsR.string.permission_denied))
            },
            text = {
                Text(
                    text = stringResource(StringsR.string.permission_multiple_denied_message)
                )
            }
        )
    }
}