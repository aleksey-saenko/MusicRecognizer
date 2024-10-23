package com.mrsep.musicrecognizer.feature.preferences.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.feature.preferences.domain.AudioCaptureMode
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@Composable
internal fun AudioSourceDialog(
    defaultAudioCaptureMode: AudioCaptureMode,
    mainButtonLongPressAudioCaptureMode: AudioCaptureMode,
    onChangeDefaultAudioCaptureMode: (AudioCaptureMode) -> Unit,
    onChangeMainButtonLongPressAudioCaptureMode: (AudioCaptureMode) -> Unit,
    onDismissClick: () -> Unit,
) {
    AlertDialog(
        title = {
            Text(text = stringResource(StringsR.string.audio_source_dialog_title))
        },
        confirmButton = {
            TextButton(onClick = onDismissClick) {
                Text(text = stringResource(StringsR.string.close))
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .width(IntrinsicSize.Min)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(text = stringResource(StringsR.string.audio_source_dialog_default_mode_message))
                Spacer(Modifier.height(16.dp))
                AudioCaptureModeDropdownMenu(
                    modes = allOptions,
                    label = stringResource(StringsR.string.audio_source_dialog_default_mode_title),
                    selectedMode = defaultAudioCaptureMode,
                    onSelectMode = onChangeDefaultAudioCaptureMode,
                )
                Spacer(Modifier.height(16.dp))
                Text(text = stringResource(StringsR.string.audio_source_dialog_button_long_press_mode_message))
                Spacer(Modifier.height(16.dp))
                AudioCaptureModeDropdownMenu(
                    modes = allOptions,
                    label = stringResource(StringsR.string.audio_source_dialog_button_long_press_mode_title),
                    selectedMode = mainButtonLongPressAudioCaptureMode,
                    onSelectMode = onChangeMainButtonLongPressAudioCaptureMode,
                )
            }
        },
        onDismissRequest = onDismissClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AudioCaptureModeDropdownMenu(
    modifier: Modifier = Modifier,
    label: String,
    modes: ImmutableList<AudioCaptureMode>,
    selectedMode: AudioCaptureMode,
    onSelectMode: (AudioCaptureMode) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedMode.getTitle(),
            onValueChange = {},
            readOnly = true,
            label = { Text(text = label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            singleLine = true,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = MaterialTheme.shapes.small,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        ) {
            modes.forEach { selectedMode ->
                DropdownMenuItem(
                    text = { Text(selectedMode.getTitle()) },
                    onClick = {
                        onSelectMode(selectedMode)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

private val allOptions = AudioCaptureMode.entries.toImmutableList()

@Composable
private fun AudioCaptureMode.getTitle(): String {
    return when (this) {
        AudioCaptureMode.Microphone -> stringResource(StringsR.string.audio_capture_mode_microphone)
        AudioCaptureMode.Device -> stringResource(StringsR.string.audio_capture_mode_device)
        AudioCaptureMode.Auto -> stringResource(StringsR.string.audio_capture_mode_auto)
    }
}
