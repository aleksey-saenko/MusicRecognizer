package com.mrsep.musicrecognizer.feature.preferences.presentation.experimental

import android.net.Uri
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.feature.preferences.domain.AppBackupManager
import com.mrsep.musicrecognizer.feature.preferences.domain.BackupEntry
import com.mrsep.musicrecognizer.feature.preferences.domain.BackupMetadataResult
import com.mrsep.musicrecognizer.feature.preferences.domain.BackupResult
import com.mrsep.musicrecognizer.feature.preferences.domain.RestoreResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import javax.inject.Inject

/* Work in progress */
@HiltViewModel
internal class ExperimentalFeaturesViewModel @Inject constructor(
    private val appBackupManager: AppBackupManager,
) : ViewModel() {

    private val _backupUiState = MutableStateFlow<BackupUiState?>(null)
    val backupState = _backupUiState.asStateFlow()

    private val _restoreUiState = MutableStateFlow<RestoreUiState?>(null)
    val restoreUiState = _restoreUiState.asStateFlow()

    private val backupMasterJob = SupervisorJob()
    private val backupScope = viewModelScope + backupMasterJob

    private val restoreMasterJob = SupervisorJob()
    private val restoreScope = viewModelScope + backupMasterJob

    /* Backup */

    fun estimateEntriesToBackup(uri: Uri) {
        if (backupMasterJob.children.any { !it.isCompleted }) return
        if (_backupUiState.value != null) return
        backupScope.launch {
            _backupUiState.update { BackupUiState.EstimatingEntries(uri) }
            val entries = appBackupManager.estimateAppDataSize()
            _backupUiState.update {
                BackupUiState.Ready(
                    uri = uri,
                    entriesUncompressedSize = entries
                )
            }
        }
    }

    fun backup(uri: Uri, entries: Set<BackupEntry>) {
        if (backupMasterJob.children.any { !it.isCompleted }) return
        if (_backupUiState.value !is BackupUiState.Ready) return
        backupScope.launch {
            _backupUiState.update { BackupUiState.InProgress(uri) }
            val backupResult = appBackupManager.backup(uri, entries)
            _backupUiState.update { BackupUiState.Result(uri, backupResult) }
        }
    }

    fun cancelBackupScopeJobs() {
        viewModelScope.launch {
            backupMasterJob.children.forEach { it.cancelAndJoin() }
            _backupUiState.update { null }
        }
    }

    /* Restore */

    fun validateBackup(uri: Uri) {
        if (restoreMasterJob.children.any { !it.isCompleted }) return
        if (_restoreUiState.value != null) return
        restoreScope.launch {
            _restoreUiState.update { RestoreUiState.ValidatingBackup(uri) }
            val metadataResult = appBackupManager.readBackupMetadata(uri)
            _restoreUiState.update { RestoreUiState.BackupMetadata(uri, metadataResult) }
        }
    }

    fun restore(uri: Uri, entries: Set<BackupEntry>) {
        if (restoreMasterJob.children.any { !it.isCompleted }) return
        val backupMetadata = (restoreUiState.value as? RestoreUiState.BackupMetadata)
            ?.result as? BackupMetadataResult.Success ?: return
        check(backupMetadata.entryUncompressedSize.keys.containsAll(entries))
        restoreScope.launch {
            _restoreUiState.update { RestoreUiState.InProgress(uri) }
            val restoreResult = appBackupManager.restore(uri, entries)
            _restoreUiState.update { RestoreUiState.Result(uri, restoreResult) }
        }
    }

    fun cancelRestoreScopeJobs() {
        viewModelScope.launch {
            restoreMasterJob.children.forEach { it.cancelAndJoin() }
            _restoreUiState.update { null }
        }
    }
}

@Stable
internal sealed class RestoreUiState {
    abstract val uri: Uri

    data class ValidatingBackup(override val uri: Uri) : RestoreUiState()

    data class BackupMetadata(
        override val uri: Uri,
        val result: BackupMetadataResult,
    ) : RestoreUiState()

    data class InProgress(override val uri: Uri) : RestoreUiState()

    data class Result(
        override val uri: Uri,
        val result: RestoreResult,
    ) : RestoreUiState()
}

@Stable
internal sealed class BackupUiState {
    abstract val uri: Uri

    data class EstimatingEntries(override val uri: Uri) : BackupUiState()

    data class Ready(
        override val uri: Uri,
        val entriesUncompressedSize: Map<BackupEntry, Long>,
    ) : BackupUiState()

    data class InProgress(override val uri: Uri) : BackupUiState()

    data class Result(
        override val uri: Uri,
        val result: BackupResult,
    ) : BackupUiState()
}
