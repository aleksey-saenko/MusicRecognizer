package com.mrsep.musicrecognizer.feature.recognition.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR

@Composable
internal fun RecordErrorDialog(
    modifier: Modifier = Modifier,
    onDismissClick: () -> Unit,
    onNavigateToAppInfo: () -> Unit
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissClick,
        confirmButton = {
            TextButton(onClick = onNavigateToAppInfo) {
                Text(text = stringResource(StringsR.string.check_permission))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissClick) {
                Text(text = stringResource(StringsR.string.dismiss))
            }
        },
        icon = {
            Icon(
                painter = painterResource(UiR.drawable.ic_error_filled_24),
                modifier = Modifier.size(48.dp),
                contentDescription = null
            )
        },
        title = {
            Text(
                text = stringResource(StringsR.string.recording_error),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = stringResource(StringsR.string.message_record_error),
                textAlign = TextAlign.Center
            )
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            securePolicy = SecureFlagPolicy.Inherit,
            usePlatformDefaultWidth = true,
            decorFitsSystemWindows = true
        )
    )
}