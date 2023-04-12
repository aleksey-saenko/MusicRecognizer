package com.mrsep.musicrecognizer.feature.recognition.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR

@Composable
internal fun UnhandledErrorDialog(
    modifier: Modifier = Modifier,
    throwable: Throwable? = null,
    onDismissClick: () -> Unit,
    onSendReportClick: () -> Unit
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissClick,
        confirmButton = {
            TextButton(onClick = onSendReportClick) {
                Text(text = stringResource(StringsR.string.send_report))
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
                text = stringResource(StringsR.string.internal_error),
                textAlign = TextAlign.Center
            )
        },
        text = {
            throwable?.let {
                Column(modifier = Modifier.animateContentSize()) {
                    var extended by rememberSaveable { mutableStateOf(false) }
                    Text(
                        text = stringResource(StringsR.string.message_unhandled_error),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    TextButton(
                        onClick = { extended = !extended },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = if (extended) {
                                stringResource(StringsR.string.show_less)
                            } else {
                                stringResource(StringsR.string.show_more)
                            }
                        )
                    }
                    if (extended) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = throwable.stackTraceToString(),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.Green
                                ),
                                modifier = Modifier
                                    .verticalScroll(rememberScrollState())
                                    .horizontalScroll(rememberScrollState())
                                    .padding(16.dp)
                            )
                        }
                    }
                }
            } ?: Text(
                text = stringResource(StringsR.string.message_unhandled_error),
                textAlign = TextAlign.Center
            )
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            securePolicy = SecureFlagPolicy.Inherit,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = true
        )
    )
}