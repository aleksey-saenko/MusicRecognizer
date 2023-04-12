package com.mrsep.musicrecognizer.feature.onboarding.presentation

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun PermissionsPage(
    modifier: Modifier = Modifier,
    onPermissionsGranted: () -> Unit
) {
    var firstShowing by rememberSaveable { mutableStateOf(true) }
    val recorderPermissionState = rememberPermissionState(
        Manifest.permission.RECORD_AUDIO
    )
    Column(
        modifier = modifier.padding(PaddingValues(horizontal = 24.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(StringsR.string.permissions),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(PaddingValues(vertical = 24.dp))
        )
        Text(
            text = stringResource(StringsR.string.onboarding_permission_message),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .widthIn(max = 488.dp)
                .padding(bottom = 24.dp)
        )
        val buttonModifier = Modifier
            .padding(bottom = 24.dp)
            .widthIn(min = 240.dp)
        if (recorderPermissionState.status.isGranted) {
            Button(
                modifier = buttonModifier,
                enabled = false,
                onClick = { }
            ) {
                Text(text = stringResource(StringsR.string.permissions_granted))
            }
            LaunchedEffect(Unit) {
                onPermissionsGranted()
            }
        } else {
            if (firstShowing || recorderPermissionState.status.shouldShowRationale) {
                Button(
                    modifier = buttonModifier,
                    enabled = true,
                    onClick = {
                        if (!recorderPermissionState.status.isGranted) {
                            recorderPermissionState.launchPermissionRequest()
                        }
                        firstShowing = false
                    }
                ) {
                    Text(text = stringResource(StringsR.string.allow_access))
                }
            } else {
                var showDialog by rememberSaveable { mutableStateOf(false) }
                Button(
                    modifier = buttonModifier,
                    enabled = true,
                    onClick = { showDialog = true }
                ) {
                    Text(text = stringResource(StringsR.string.allow_access))
                }
                if (showDialog) {
                    DialogForOpeningAppSettings(
                        onConfirmClick = { showDialog = false },
                        onDismissClick = { showDialog = false }
                    )
                }
            }
        }
    }

}

@Composable
internal fun DialogForOpeningAppSettings(
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