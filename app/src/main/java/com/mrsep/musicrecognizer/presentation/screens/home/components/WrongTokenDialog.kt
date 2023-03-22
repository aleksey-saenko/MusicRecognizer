package com.mrsep.musicrecognizer.presentation.screens.home.components

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
import com.mrsep.musicrecognizer.R

@Composable
fun WrongTokenDialog(
    isLimitReached: Boolean,
    modifier: Modifier = Modifier,
    onDismissClick: () -> Unit,
    onNavigateToPreferences: () -> Unit
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissClick,
        confirmButton = {
            TextButton(onClick = onNavigateToPreferences) {
                Text(text = stringResource(R.string.preferences))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissClick) {
                Text(text = stringResource(R.string.dismiss))
            }
        },
        icon = {
            Icon(
                painter = painterResource(R.drawable.baseline_key_off_24),
                modifier = Modifier.size(48.dp),
                contentDescription = null
            )
        },
        title = {
            Text(
                text = if (isLimitReached) {
                    stringResource(R.string.token_limit_reached)
                } else {
                    stringResource(R.string.wrong_token)
                },
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = stringResource(R.string.message_token_wrong_error),
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