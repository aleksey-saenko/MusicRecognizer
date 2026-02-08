package com.mrsep.musicrecognizer.feature.backup.presentation

import android.net.Uri
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.feature.backup.AppBackupManager
import com.mrsep.musicrecognizer.feature.backup.AppRestartManager
import com.mrsep.musicrecognizer.feature.backup.BackupEntry
import com.mrsep.musicrecognizer.feature.backup.BackupMetadataResult
import com.mrsep.musicrecognizer.feature.backup.BackupResult
import com.mrsep.musicrecognizer.feature.backup.RestoreResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import javax.inject.Inject

/* Work in progress */
@HiltViewModel
internal class ExperimentalFeaturesViewModel @Inject constructor(
    private val appBackupManager: AppBackupManager,
    private val appRestartManager: AppRestartManager,
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

    fun estimateEntriesToBackup() {
        if (backupMasterJob.children.any { !it.isCompleted }) return
        if (_backupUiState.value != null) return
        backupScope.launch {
            _backupUiState.value = BackupUiState.EstimatingEntries
            val entries = appBackupManager.estimateAppDataSize()
            _backupUiState.value = BackupUiState.Ready(
                entriesUncompressedSize = entries,
                selectedEntries = entries.keys
            )
        }
    }

    fun backup(uri: Uri) {
        if (backupMasterJob.children.any { !it.isCompleted }) return
        val currentState = _backupUiState.value
        if (currentState !is BackupUiState.Ready) return
        if (currentState.selectedEntries.isEmpty()) return
        backupScope.launch {
            _backupUiState.value = BackupUiState.InProgress(uri)
            val backupResult = appBackupManager.backup(uri, currentState.selectedEntries)
            _backupUiState.value = BackupUiState.Result(uri, backupResult)
        }
    }

    fun cancelBackupScopeJobs() {
        viewModelScope.launch {
            backupMasterJob.children.forEach { it.cancelAndJoin() }
            _backupUiState.value = null
        }
    }

    /* Restore */

    fun validateBackup(uri: Uri) {
        if (restoreMasterJob.children.any { !it.isCompleted }) return
        if (_restoreUiState.value != null) return
        restoreScope.launch {
            _restoreUiState.value = RestoreUiState.ValidatingBackup(uri)
            val metadataResult = appBackupManager.readBackupMetadata(uri)
            _restoreUiState.value = RestoreUiState.Ready(
                uri = uri,
                metadata = metadataResult,
                selectedEntries = when (metadataResult) {
                    is BackupMetadataResult.Success -> metadataResult.entryUncompressedSize.keys
                    else -> emptySet()
                }
            )
        }
    }

    fun restore(uri: Uri) {
        if (restoreMasterJob.children.any { !it.isCompleted }) return
        val currentState = restoreUiState.value
        if (currentState !is RestoreUiState.Ready) return
        val metadata = currentState.metadata as? BackupMetadataResult.Success ?: return
        if (currentState.selectedEntries.isEmpty()) return
        check(metadata.entryUncompressedSize.keys.containsAll(currentState.selectedEntries))
        restoreScope.launch {
            _restoreUiState.value = RestoreUiState.InProgress(uri)
            val restoreResult = appBackupManager.restore(uri, currentState.selectedEntries)
            _restoreUiState.value = RestoreUiState.Result(uri, restoreResult)
        }
    }

    fun cancelRestoreScopeJobs() {
        viewModelScope.launch {
            restoreMasterJob.children.forEach { it.cancelAndJoin() }
            _restoreUiState.value = null
        }
    }

    fun restartApplicationOnRestore() {
        appRestartManager.restartApplicationOnRestore()
    }

    fun onChangeSelectedBackupEntry(entry: BackupEntry, selected: Boolean) {
        val currentState = _backupUiState.value
        val currentSelectedEntries = when (currentState) {
            is BackupUiState.Ready -> currentState.selectedEntries
            else -> return
        }
        _backupUiState.value = currentState.copy(
            selectedEntries = currentSelectedEntries
                .run { if (selected) plus(entry) else minus(entry) }
        )
    }

    fun onChangeSelectedRestoreEntry(entry: BackupEntry, selected: Boolean) {
        val currentState = _restoreUiState.value
        val currentSelectedEntries = when (currentState) {
            is RestoreUiState.Ready -> currentState.selectedEntries
            else -> return
        }
        _restoreUiState.value = currentState.copy(
            selectedEntries = currentSelectedEntries
                .run { if (selected) plus(entry) else minus(entry) }
        )
    }
}

@Stable
internal sealed class RestoreUiState {
    abstract val uri: Uri

    data class ValidatingBackup(override val uri: Uri) : RestoreUiState()

    data class Ready(
        override val uri: Uri,
        val metadata: BackupMetadataResult,
        val selectedEntries: Set<BackupEntry>,
    ) : RestoreUiState()

    data class InProgress(override val uri: Uri) : RestoreUiState()

    data class Result(
        override val uri: Uri,
        val result: RestoreResult,
    ) : RestoreUiState()
}

@Stable
internal sealed class BackupUiState {

    data object EstimatingEntries : BackupUiState()

    data class Ready(
        val entriesUncompressedSize: Map<BackupEntry, Long>,
        val selectedEntries: Set<BackupEntry>,
    ) : BackupUiState()

    data class InProgress(val uri: Uri) : BackupUiState()

    data class Result(
        val uri: Uri,
        val result: BackupResult,
    ) : BackupUiState()
}
