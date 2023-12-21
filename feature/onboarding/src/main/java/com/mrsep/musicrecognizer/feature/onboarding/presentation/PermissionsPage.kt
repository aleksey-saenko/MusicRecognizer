package com.mrsep.musicrecognizer.feature.onboarding.presentation

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.mrsep.musicrecognizer.core.ui.components.RecorderPermissionBlockedDialog
import com.mrsep.musicrecognizer.core.ui.components.RecorderPermissionRationaleDialog
import com.mrsep.musicrecognizer.core.ui.findActivity
import com.mrsep.musicrecognizer.core.ui.shouldShowRationale
import com.mrsep.musicrecognizer.core.ui.util.openUrlImplicitly
import com.mrsep.musicrecognizer.core.strings.R as StringsR

private const val TERMS_ANNOTATION_TAG = "TERMS_TAG"

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
        if (!granted && !context.findActivity()
                .shouldShowRationale(Manifest.permission.RECORD_AUDIO)
        ) {
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
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(PaddingValues(top = 24.dp))
        )
        val annotatedText = buildAnnotatedString {
            append(stringResource(StringsR.string.onboarding_permission_message_start))
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                pushStringAnnotation(
                    tag = TERMS_ANNOTATION_TAG,
                    annotation = stringResource(StringsR.string.audd_terms_link)
                )
                append(stringResource(StringsR.string.onboarding_permission_message_link))
            }
            append(stringResource(StringsR.string.onboarding_permission_message_end))
        }
        ClickableText(
            text = annotatedText,
            style = MaterialTheme.typography.bodyLarge.copy(
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            ),
            onClick = { offset ->
                annotatedText.getStringAnnotations(
                    tag = TERMS_ANNOTATION_TAG,
                    start = offset,
                    end = offset
                ).firstOrNull()?.item?.let { link ->
                    context.openUrlImplicitly(link)
                }
            },
            modifier = Modifier
                .widthIn(max = 488.dp)
                .padding(top = 24.dp)
        )
        Button(
            modifier = Modifier
                .padding(top = 24.dp)
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
                text = stringResource(
                    if (recorderPermissionState.status.isGranted) {
                        StringsR.string.permission_granted
                    } else {
                        StringsR.string.request_permission
                    }
                )
            )
        }
    }

}