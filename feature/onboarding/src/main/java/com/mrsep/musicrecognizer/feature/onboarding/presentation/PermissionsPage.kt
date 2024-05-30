package com.mrsep.musicrecognizer.feature.onboarding.presentation

import android.Manifest
import android.os.Build
import androidx.compose.foundation.background
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
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.mrsep.musicrecognizer.core.ui.components.RecognitionPermissionsBlockedDialog
import com.mrsep.musicrecognizer.core.ui.components.RecognitionPermissionsRationaleDialog
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
    // region <permission handling block>
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
            onPermissionsGranted()
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
    // endregion

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(StringsR.string.permissions),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineLarge
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
        Spacer(Modifier.height(24.dp))
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
            modifier = Modifier.widthIn(max = 488.dp)
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(StringsR.string.notificaiton_permission_rationale_message),
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 488.dp)
            )
        }
        Spacer(Modifier.height(24.dp))
        Button(
            modifier = Modifier.widthIn(min = 240.dp),
            enabled = !requiredPermissionsState.allPermissionsGranted,
            onClick = {
                if (requiredPermissionsState.shouldShowRationale) {
                    showPermissionsRationaleDialog = true
                } else {
                    requiredPermissionsState.launchMultiplePermissionRequest()
                }
            }
        ) {
            Text(
                text = stringResource(
                    if (requiredPermissionsState.allPermissionsGranted) {
                        StringsR.string.permission_granted
                    } else {
                        StringsR.string.request_permission
                    }
                )
            )
        }
    }
}