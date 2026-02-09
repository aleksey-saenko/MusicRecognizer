package com.mrsep.musicrecognizer.feature.backup.presentation

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.mrsep.musicrecognizer.core.common.util.getAppVersionCode
import com.mrsep.musicrecognizer.feature.backup.BackupEntry
import com.mrsep.musicrecognizer.feature.backup.BackupMetadataResult
import com.mrsep.musicrecognizer.feature.backup.RestoreResult
import kotlinx.coroutines.delay
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@Composable
internal fun RestoreDialog(
    restoreState: RestoreUiState,
    onChangeSelectedBackupEntry: (entry: BackupEntry, selected: Boolean) -> Unit,
    onAppRestartRequest: () -> Unit,
    onRestoreClick: (Uri) -> Unit,
    onDismissRequest: (() -> Unit)?,
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    var restoreConfirmed by remember { mutableStateOf(false) }
    LaunchedEffect(restoreConfirmed) {
        if (restoreConfirmed) {
            delay(3000)
            restoreConfirmed = false
        }
    }
    val dismissOnClickOutside = restoreState !is RestoreUiState.InProgress
    AlertDialog(
        title = {
            Text(text = stringResource(StringsR.string.restore_dialog_title))
        },
        confirmButton = {
            if (restoreState is RestoreUiState.Ready) {
                TextButton(
                    enabled = restoreState.selectedEntries.isNotEmpty(),
                    onClick = {
                        if (restoreConfirmed) {
                            onRestoreClick(restoreState.uri)
                        } else {
                            restoreConfirmed = true
                            val message = resources.getString(StringsR.string.toast_press_again_to_confirm)
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    },
                ) {
                    Text(text = stringResource(StringsR.string.restore_dialog_button_restore))
                }
            }
        },
        dismissButton = {
            if (onDismissRequest != null) {
                TextButton(onClick = onDismissRequest) {
                    Text(text = stringResource(StringsR.string.close))
                }
            }
        },
        onDismissRequest = { onDismissRequest?.invoke() },
        properties = DialogProperties(
            dismissOnBackPress = dismissOnClickOutside,
            dismissOnClickOutside = dismissOnClickOutside,
        ),
        text = {
            Column(Modifier.fillMaxWidth()) {
                when (restoreState) {
                    is RestoreUiState.ValidatingBackup -> DialogProgressRow(
                        title = stringResource(StringsR.string.restore_checking_backup)
                    )

                    is RestoreUiState.Ready -> {
                        when (restoreState.metadata) {
                            is BackupMetadataResult.Success -> Column {
                                if (context.getAppVersionCode() < restoreState.metadata.metadata.appVersionCode) {
                                    Text(stringResource(StringsR.string.restore_result_newer_version))
                                } else {
                                    BackupEntryPicker(
                                        title = stringResource(StringsR.string.restore_entry_picker_title),
                                        availableEntriesWithSizes = restoreState.metadata.entryUncompressedSize,
                                        selectedBackupEntries = restoreState.selectedEntries,
                                        onChangeSelectedBackupEntry = onChangeSelectedBackupEntry
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    RestoreInfoMessage(modifier = Modifier.fillMaxWidth())
                                }
                            }

                            BackupMetadataResult.FileNotFound -> Text(
                                text = stringResource(StringsR.string.backup_restore_result_file_not_found)
                            )

                            BackupMetadataResult.MalformedBackup -> Text(
                                text = stringResource(StringsR.string.restore_result_malformed_backup)
                            )

                            BackupMetadataResult.NotBackupFile -> Text(
                                text = stringResource(StringsR.string.restore_result_not_backup)
                            )

                            BackupMetadataResult.UnhandledError -> Text(
                                text = stringResource(StringsR.string.backup_restore_unhandled_error)
                            )
                        }
                    }

                    is RestoreUiState.InProgress -> DialogProgressRow(
                        title = stringResource(StringsR.string.restore_in_progress),
                        subtitle = stringResource(StringsR.string.restore_info_restart)
                    )

                    is RestoreUiState.Result -> when (restoreState.result) {
                        is RestoreResult.Success -> {
                            if (restoreState.result.appRestartRequired) {
                                var remaining by remember { mutableIntStateOf(3) }
                                LaunchedEffect(Unit) {
                                    while (remaining > 0) {
                                        delay(1000)
                                        remaining--
                                    }
                                    onAppRestartRequest()
                                }
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(stringResource(StringsR.string.restore_result_success))
                                    Text(stringResource(StringsR.string.restore_will_restart_in).format(remaining))
                                }
                            } else {
                                Text(stringResource(StringsR.string.restore_result_success))
                            }
                        }

                        RestoreResult.FileNotFound,
                        RestoreResult.MalformedBackup,
                        RestoreResult.NewerVersionBackup,
                        RestoreResult.NotBackupFile,
                        RestoreResult.UnhandledError,
                        -> Text(text = restoreState.result.getMessage())
                    }
                }
            }
        },
    )
}

@Composable
private fun RestoreInfoMessage(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = stringResource(StringsR.string.restore_erase_warning))
        Text(text = stringResource(StringsR.string.restore_info_restart))
    }
}

@Composable
private fun RestoreResult.getMessage() = when (this) {
    is RestoreResult.Success -> stringResource(StringsR.string.restore_result_success)
    RestoreResult.FileNotFound -> stringResource(StringsR.string.backup_restore_result_file_not_found)
    RestoreResult.MalformedBackup -> stringResource(StringsR.string.restore_result_malformed_backup)
    RestoreResult.NewerVersionBackup -> stringResource(StringsR.string.restore_result_newer_version)
    RestoreResult.NotBackupFile -> stringResource(StringsR.string.restore_result_not_backup)
    RestoreResult.UnhandledError -> stringResource(StringsR.string.backup_restore_unhandled_error) +
            "\n" + stringResource(StringsR.string.backup_restore_unhandled_error_message)
}
