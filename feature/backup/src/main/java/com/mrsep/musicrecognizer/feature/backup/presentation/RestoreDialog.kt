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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.mrsep.musicrecognizer.core.common.util.getAppVersionCode
import com.mrsep.musicrecognizer.feature.backup.BackupEntry
import com.mrsep.musicrecognizer.feature.backup.BackupMetadataResult
import com.mrsep.musicrecognizer.feature.backup.RestoreResult
import kotlinx.coroutines.delay
import com.mrsep.musicrecognizer.core.strings.R as StringsR

/* Work in progress */
@Composable
internal fun RestoreDialog(
    restoreState: RestoreUiState,
    onAppRestartRequest: () -> Unit,
    onRestoreClick: (Uri, Set<BackupEntry>) -> Unit,
    onDismissRequest: (() -> Unit)?,
) {
    val context = LocalContext.current
    var selectedBackupEntries by rememberSaveable(stateSaver = BackupEntriesSaver) {
        mutableStateOf(emptySet())
    }
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
            if (restoreState is RestoreUiState.BackupMetadata) {
                TextButton(
                    enabled = selectedBackupEntries.isNotEmpty(),
                    onClick = {
                        if (restoreConfirmed) {
                            onRestoreClick(restoreState.uri, selectedBackupEntries)
                        } else {
                            restoreConfirmed = true
                            Toast.makeText(
                                context,
                                "Press again to confirm action",
                                Toast.LENGTH_SHORT
                            ).show()
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
                        title = "Checking backup file, please wait…"
                    )

                    is RestoreUiState.BackupMetadata -> {
                        when (restoreState.result) {
                            is BackupMetadataResult.Success -> Column {
                                if (context.getAppVersionCode() < restoreState.result.metadata.appVersionCode) {
                                    Text("This backup file cannot be used because it was created by a newer app version")
                                } else {
                                    BackupEntryPicker(
                                        title = "Select what do you want to restore:",
                                        availableEntriesWithSizes = restoreState.result.entryUncompressedSize,
                                        selectedBackupEntries = selectedBackupEntries,
                                        onChangeSelectedBackupEntry = {
                                            selectedBackupEntries = it
                                        }
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    RestoreInfoMessage(modifier = Modifier.fillMaxWidth())
                                }
                            }

                            BackupMetadataResult.FileNotFound -> Text(
                                text = "The backup file was not found!"
                            )

                            BackupMetadataResult.MalformedBackup -> Text(
                                text = "This backup file is corrupted!"
                            )

                            BackupMetadataResult.NotBackupFile -> Text(
                                text = "This file is not recognized as a backup!"
                            )

                            BackupMetadataResult.UnhandledError -> Text(
                                text = "An unhandled error occurred!"
                            )
                        }
                    }

                    is RestoreUiState.InProgress -> DialogProgressRow(
                        title = "Restoring, please wait…\nThe app will restart upon completion."
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
                                    Text("Success!")
                                    Text("The app will be restarted in $remaining seconds…")
                                }
                            } else {
                                Text("Success!")
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
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = "The restore process will erase current data and replace it with backup files." +
                    " This action cannot be undone. Back up important data before proceeding."
        )
        Text(text = "The app will restart once the restore is complete.")
    }
}

private fun RestoreResult.getMessage() = when (this) {
    is RestoreResult.Success -> "Success!"
    RestoreResult.FileNotFound -> "The backup file was not found!"
    RestoreResult.MalformedBackup -> "This backup file is corrupted!"
    RestoreResult.NewerVersionBackup -> "This backup file is created by newer app version!"
    RestoreResult.NotBackupFile -> "This file is not recognized as a backup!"
    RestoreResult.UnhandledError -> "An unhandled error occurred!" +
            "\nPlease check that there is enough free space on your device and try again."
}
