package com.mrsep.musicrecognizer.presentation.screens.home.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import com.mrsep.musicrecognizer.R

@Composable
fun HttpErrorDialog(
    code: Int,
    message: String,
    modifier: Modifier = Modifier,
    onDismissClick: () -> Unit,
    onSendReportClick: () -> Unit
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissClick,
        confirmButton = {
            TextButton(onClick = onSendReportClick) {
                Text(text = stringResource(R.string.send_report))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissClick) {
                Text(text = stringResource(R.string.dismiss))
            }
        },
        icon = {
            Icon(
                painter = painterResource(R.drawable.ic_error_filled_24),
                modifier = Modifier.size(48.dp),
                contentDescription = null
            )
        },
        title = {
            Text(
                text = "Bad network response",
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(modifier = Modifier.animateContentSize()) {
                Text(
                    text = stringResource(R.string.message_http_error),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Text(
                    text = "Code: ${code}\nMessage: $message",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(top = 24.dp)
                )
            }
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