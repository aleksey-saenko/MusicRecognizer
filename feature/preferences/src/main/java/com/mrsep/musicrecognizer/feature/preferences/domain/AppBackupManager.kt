package com.mrsep.musicrecognizer.feature.preferences.domain

import android.net.Uri
import java.time.Instant

interface AppBackupManager {

    suspend fun estimateAppDataSize(): Map<BackupEntry, Long>

    suspend fun readBackupMetadata(source: Uri): BackupMetadataResult

    suspend fun backup(destination: Uri, entries: Set<BackupEntry>): BackupResult

    suspend fun restore(source: Uri, entries: Set<BackupEntry>): RestoreResult
}

enum class BackupEntry { Data, Preferences }

sealed class BackupResult {
    data object Success : BackupResult()

    data object FileNotFound : BackupResult()
    data object UnhandledError : BackupResult()
}

sealed class BackupMetadataResult {
    data class Success(
        val appVersionCode: Int,
        val creationDate: Instant,
        val entryUncompressedSize: Map<BackupEntry, Long>,
    ) : BackupMetadataResult()

    data object NotBackupFile : BackupMetadataResult()
    data object MalformedBackup : BackupMetadataResult()

    data object FileNotFound : BackupMetadataResult()
    data object UnhandledError : BackupMetadataResult()
}

sealed class RestoreResult {
    data class Success(val appRestartRequired: Boolean) : RestoreResult()

    data object NewerVersionBackup : RestoreResult()

    data object NotBackupFile : RestoreResult()
    data object MalformedBackup : RestoreResult()

    data object FileNotFound : RestoreResult()
    data object UnhandledError : RestoreResult()
}