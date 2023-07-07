package com.mrsep.musicrecognizer.feature.onboarding.presentation

import android.Manifest
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
import com.mrsep.musicrecognizer.core.ui.components.RecorderPermissionBlockedDialog
import com.mrsep.musicrecognizer.core.ui.components.RecorderPermissionRationaleDialog
import com.mrsep.musicrecognizer.core.ui.findActivity
import com.mrsep.musicrecognizer.core.ui.shouldShowRationale
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun PermissionsPage(
    modifier: Modifier = Modifier,
    onPermissionsGranted: () -> Unit
) {
    val context = LocalContext.current
    //region <permission handling block>
    var permissionBlockedDialogVisible by rememberSaveable { mutableStateOf(false) }
    var permissionRationaleDialogVisible by rememberSaveable { mutableStateOf(false) }
    val recorderPermissionState = rememberPermissionState(
        Manifest.permission.RECORD_AUDIO
    ) { granted ->
        if (!granted && !context.findActivity().shouldShowRationale(Manifest.permission.RECORD_AUDIO)) {
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
    LaunchedEffect(recorderPermissionState.status.isGranted) {
        if (recorderPermissionState.status.isGranted) {
            onPermissionsGranted()
        }
    }

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
        Button(
            modifier = Modifier
                .padding(bottom = 24.dp)
                .widthIn(min = 240.dp),
            enabled = !recorderPermissionState.status.isGranted,
            onClick = {
                if (recorderPermissionState.status.shouldShowRationale) {
                    permissionRationaleDialogVisible = true
                } else {
                    recorderPermissionState.launchPermissionRequest()
                }
            }
        ) {
            Text(
                text = if (recorderPermissionState.status.isGranted)
                    stringResource(StringsR.string.permissions_granted)
                else
                    stringResource(StringsR.string.allow_access)
            )
        }
    }

}