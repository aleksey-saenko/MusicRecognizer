package com.mrsep.musicrecognizer.feature.preferences.presentation

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.domain.preferences.AudioCaptureMode
import com.mrsep.musicrecognizer.core.ui.R
import com.mrsep.musicrecognizer.core.ui.components.DialogSwitch
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import com.mrsep.musicrecognizer.core.strings.R as StringsR

// TODO: finalize feature and use proper string resources

@Composable
internal fun AudioSourceDialog(
    defaultAudioCaptureMode: AudioCaptureMode,
    mainButtonLongPressAudioCaptureMode: AudioCaptureMode,
    useAltDeviceSoundSource: Boolean,
    onChangeDefaultAudioCaptureMode: (AudioCaptureMode) -> Unit,
    onChangeMainButtonLongPressAudioCaptureMode: (AudioCaptureMode) -> Unit,
    onChangeUseAltDeviceSoundSource: (Boolean) -> Unit,
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
                Spacer(Modifier.height(16.dp))
                var showAltDeviceSourceDialog by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // To capture device audio the app uses AudioPlaybackCapture API (Android 10+) by default
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        IconButton(
                            onClick = { showAltDeviceSourceDialog = true }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.outline_help_24),
                                contentDescription = stringResource(StringsR.string.help),
                            )
                        }
                        DialogSwitch(
                            modifier = Modifier.weight(1f),
                            title = "Use alternative device sound source",
                            checked = useAltDeviceSoundSource,
                            onClick = { onChangeUseAltDeviceSoundSource(!useAltDeviceSoundSource) },
                        )
                    } else {
                        Text(
                            text = "The app uses experimental support for recording device audio on Android 9 and below",
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(
                            onClick = { showAltDeviceSourceDialog = true }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.outline_help_24),
                                contentDescription = stringResource(StringsR.string.help),
                            )
                        }
                    }
                }
                if (showAltDeviceSourceDialog) {
                    AltDeviceSourceHelpDialog { showAltDeviceSourceDialog = false }
                }
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

private const val ISSUE_TRACKER = "https://github.com/aleksey-saenko/MusicRecognizer/issues"
@Composable
internal fun AltDeviceSourceHelpDialog(
    modifier: Modifier = Modifier,
    onDismissClick: () -> Unit
) {
    AlertDialog(
        modifier = modifier,
        title = {
            Text(text = stringResource(StringsR.string.audio_capture_mode_device))
        },
        confirmButton = {
            TextButton(onClick = onDismissClick) {
                Text(text = stringResource(StringsR.string.close))
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = "The app supports two methods for recording device audio:",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "1. Screen Casting (primary method)",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "• This method provides the best sound quality and is supported by all devices running Android 10 and above.")
                    Text(text = "• Some apps may implicitly block audio capture by using capture policies. In this case, Audile will receive silence.")
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "2. Alternative method (experimental)",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "• Does not require screen casting permission and bypasses app restrictions on audio capture.")
                    Text(text = "• Supported by all Android versions.")
                    Text(text = "• The sound quality is worse, and this method may not work on some devices.")
                    Text(
                        text = AnnotatedString.fromHtml(
                            htmlString = "• If you encounter distorted audio recordings, please <a href=\"$ISSUE_TRACKER\">report an issue</a>.",
                            linkStyles = TextLinkStyles(
                                style = SpanStyle(color = MaterialTheme.colorScheme.primary),
                                hoveredStyle = SpanStyle(textDecoration = TextDecoration.Underline),
                            )
                        )
                    )
                }
            }
        },
        onDismissRequest = onDismissClick
    )
}
