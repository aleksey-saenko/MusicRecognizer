package com.mrsep.musicrecognizer.feature.recognition.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR

@Composable
internal fun NoMatchFoundDialog(
    modifier: Modifier = Modifier,
    onDismissClick: () -> Unit,
    onSaveClick: () -> Unit,
    onRetryClick: () -> Unit
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissClick,
        confirmButton = {
            TextButton(onClick = onSaveClick) {
                Text(text = stringResource(StringsR.string.save))
            }
            TextButton(onClick = onRetryClick) {
                Text(text = stringResource(StringsR.string.retry))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissClick) {
                Text(text = stringResource(StringsR.string.dismiss))
            }
        },
        icon = {
            Icon(
                imageVector = Icons.Default.Search,
                modifier = Modifier.size(48.dp),
                contentDescription = null
            )
        },
        title = {
            Text(
                text = stringResource(StringsR.string.no_matches_found),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = stringResource(StringsR.string.save_record_message),
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