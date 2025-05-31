package com.mrsep.musicrecognizer.feature.recognition.presentation.queuescreen

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR

@Composable
internal fun RenameRecognitionDialog(
    initialName: String,
    modifier: Modifier = Modifier,
    onConfirmClick: (String) -> Unit,
    onDismissClick: () -> Unit
) {
    var newName by rememberSaveable { mutableStateOf(initialName) }
    AlertDialog(
        properties = DialogProperties(dismissOnClickOutside = false),
        modifier = modifier,
        onDismissRequest = onDismissClick,
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmClick(newName)
                },
                enabled = newName.isNotBlank()
            ) {
                Text(text = stringResource(StringsR.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissClick) {
                Text(text = stringResource(StringsR.string.cancel))
            }
        },
        title = {
            Text(
                text = stringResource(StringsR.string.recognition_action_rename),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                trailingIcon = {
                    Crossfade(
                        targetState = newName.isNotEmpty(),
                        label = ""
                    ) { visible ->
                        if (visible) {
                            IconButton(onClick = { newName = "" }) {
                                Icon(
                                    painter = painterResource(UiR.drawable.outline_close_24),
                                    contentDescription = stringResource(StringsR.string.clear_text_field)
                                )
                            }
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.small
            )
        }
    )
}
