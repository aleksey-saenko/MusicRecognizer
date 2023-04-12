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
internal fun BadConnectionDialog(
    modifier: Modifier = Modifier,
    onDismissClick: () -> Unit,
    onNavigateToQueue: () -> Unit
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissClick,
        confirmButton = {
            TextButton(onClick = onNavigateToQueue) {
                Text(text = stringResource(StringsR.string.recognition_queue))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissClick) {
                Text(text = stringResource(StringsR.string.dismiss))
            }
        },
        icon = {
            Icon(
                painter = painterResource(UiR.drawable.baseline_cloud_off_24),
                modifier = Modifier.size(48.dp),
                contentDescription = null
            )
        },
        title = {
            Text(
                text = stringResource(StringsR.string.no_internet_connection),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = stringResource(StringsR.string.internet_not_available),
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



