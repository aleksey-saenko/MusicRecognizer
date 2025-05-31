package com.mrsep.musicrecognizer.feature.recognition.presentation.queuescreen

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@Composable
internal fun DeleteSelectedDialog(
    onDeleteClick: () -> Unit,
    onDismissClick: () -> Unit,
    inProgress: Boolean
) {
    AlertDialog(
        properties = DialogProperties(dismissOnClickOutside = false),
        title = {
            Text(text = stringResource(StringsR.string.delete_recognitions_title))
        },
        confirmButton = {
            TextButton(onClick = onDeleteClick, enabled = !inProgress) {
                Text(text = stringResource(StringsR.string.delete))
                Crossfade(targetState = inProgress, label = "inProgress") { progress ->
                    if (progress) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(start = 8.dp).size(24.dp),
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissClick, enabled = !inProgress) {
                Text(text = stringResource(StringsR.string.cancel))
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(text = stringResource(StringsR.string.delete_recognitions_message))
            }
        },
        onDismissRequest = {
            if (!inProgress) onDismissClick()
        }
    )
}
