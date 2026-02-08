package com.mrsep.musicrecognizer.feature.backup.presentation

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.mrsep.musicrecognizer.core.ui.util.shareFile
import com.mrsep.musicrecognizer.feature.backup.BackupEntry
import com.mrsep.musicrecognizer.feature.backup.BackupResult
import com.mrsep.musicrecognizer.core.strings.R as StringsR

/* Work in progress */
@Composable
internal fun BackupDialog(
    backupState: BackupUiState,
    onChangeSelectedBackupEntry: (entry: BackupEntry, selected: Boolean) -> Unit,
    onBackupClick: () -> Unit,
    onDismissClick: () -> Unit,
) {
    val context = LocalContext.current
    val dismissOnClickOutside = backupState !is BackupUiState.InProgress
    AlertDialog(
        title = {
            Text(text = stringResource(StringsR.string.backup_dialog_title))
        },
        confirmButton = {
            when (backupState) {
                is BackupUiState.EstimatingEntries,
                is BackupUiState.InProgress -> {}

                is BackupUiState.Ready -> TextButton(
                    enabled = backupState.selectedEntries.isNotEmpty(),
                    onClick = onBackupClick
                ) {
                    Text(text = stringResource(StringsR.string.backup_dialog_button_backup))
                }

                is BackupUiState.Result -> when (backupState.result) {
                    BackupResult.Success -> TextButton(
                        enabled = true,
                        onClick = {
                            @SuppressLint("LocalContextGetResourceValueCall")
                            val subject = context.getString(StringsR.string.app_name) + " backup"
                            context.shareFile(subject, "", backupState.uri)
                        }
                    ) {
                        Text(text = stringResource(StringsR.string.share))
                    }

                    BackupResult.FileNotFound,
                    BackupResult.UnhandledError -> {}
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissClick) {
                Text(text = backupState.dismissButtonText())
            }
        },
        onDismissRequest = onDismissClick,
        properties = DialogProperties(
            dismissOnBackPress = dismissOnClickOutside,
            dismissOnClickOutside = dismissOnClickOutside,
        ),
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                when (backupState) {
                    is BackupUiState.EstimatingEntries -> {
                        DialogProgressRow(title = "Estimating app data size…")
                    }

                    is BackupUiState.Ready -> {
                        BackupEntryPicker(
                            title = "Select what do you want to backup:",
                            availableEntriesWithSizes = backupState.entriesUncompressedSize,
                            selectedBackupEntries = backupState.selectedEntries,
                            onChangeSelectedBackupEntry = onChangeSelectedBackupEntry,
                        )
                    }

                    is BackupUiState.InProgress -> {
                        DialogProgressRow(title = "The backup is creating, please wait…")
                    }

                    is BackupUiState.Result -> {
                        Text(backupState.result.getMessage())
                    }
                }
            }
        },
    )
}

@Composable
internal fun BackupEntryPicker(
    title: String,
    availableEntriesWithSizes: Map<BackupEntry, Long>,
    selectedBackupEntries: Set<BackupEntry>,
    onChangeSelectedBackupEntry: (entry: BackupEntry, selected: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Column(modifier = modifier) {
        Text(text = title)
        Spacer(Modifier.height(12.dp))
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            availableEntriesWithSizes.forEach { (backupEntry, entrySize) ->
                val size = remember(entrySize) {
                    context.funFormatByteSize(entrySize)
                }
                val checked = selectedBackupEntries.contains(backupEntry)
                BackupEntryCheckBox(
                    title = backupEntry.getTitle(),
                    subtitle = size,
                    checked = checked,
                    onClick = { onChangeSelectedBackupEntry(backupEntry, !checked) }
                )
            }
        }
    }
}

private fun BackupResult.getMessage() = when (this) {
    BackupResult.Success -> "The backup file was created successfully!"
    BackupResult.FileNotFound -> "The backup file was not found!"
    BackupResult.UnhandledError -> "An unhandled error occurred!" +
            "\nPlease check that there is enough free space on your device and try again."
}

@Composable
private fun BackupUiState.dismissButtonText() = when (this) {
    is BackupUiState.EstimatingEntries,
    is BackupUiState.InProgress,
    -> stringResource(StringsR.string.cancel)

    is BackupUiState.Ready,
    is BackupUiState.Result,
    -> stringResource(StringsR.string.close)
}

@Composable
internal fun DialogProgressRow(
    modifier: Modifier = Modifier,
    title: String,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f)
        )
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 3.dp,
            strokeCap = StrokeCap.Round,
        )
    }
}

@Composable
internal fun BackupEntryCheckBox(
    title: String,
    subtitle: String,
    checked: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,

        ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { onClick() }
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.alpha(0.85f)
            )
        }
    }
}

internal fun Context.funFormatByteSize(bytes: Long): String {
    return android.text.format.Formatter.formatShortFileSize(this, bytes)
}

internal fun BackupEntry.getTitle() = when (this) {
    BackupEntry.Data -> "Database with recordings"
    BackupEntry.Preferences -> "Preferences"
}
