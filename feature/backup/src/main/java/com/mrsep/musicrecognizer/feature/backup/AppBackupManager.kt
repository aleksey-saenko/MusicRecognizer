package com.mrsep.musicrecognizer.feature.backup

import android.net.Uri
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.Instant

internal interface AppBackupManager {

    suspend fun estimateAppDataSize(): Map<BackupEntry, Long>

    suspend fun readBackupMetadata(source: Uri): BackupMetadataResult

    suspend fun backup(destination: Uri, entries: Set<BackupEntry>): BackupResult

    suspend fun restore(source: Uri, entries: Set<BackupEntry>): RestoreResult
}

//@Keep
@JsonClass(generateAdapter = false)
internal enum class BackupEntry { Data, Preferences }

@JsonClass(generateAdapter = true)
internal data class BackupMetadata(
    @Json(name = "backupSignature")
    val backupSignature: String,
    @Json(name = "appVersionCode")
    val appVersionCode: Int,
    @Json(name = "creationDate")
    val creationDate: Instant,
    @Json(name = "entries")
    val entries: Set<BackupEntry>,
)

internal sealed class BackupResult {
    data object Success : BackupResult()

    data object FileNotFound : BackupResult()
    data object UnhandledError : BackupResult()
}

internal sealed class BackupMetadataResult {
    data class Success(
        val metadata: BackupMetadata,
        val entryUncompressedSize: Map<BackupEntry, Long>,
    ) : BackupMetadataResult()

    data object NotBackupFile : BackupMetadataResult()
    data object MalformedBackup : BackupMetadataResult()

    data object FileNotFound : BackupMetadataResult()
    data object UnhandledError : BackupMetadataResult()
}

internal sealed class RestoreResult {
    data class Success(val appRestartRequired: Boolean) : RestoreResult()

    data object NewerVersionBackup : RestoreResult()

    data object NotBackupFile : RestoreResult()
    data object MalformedBackup : RestoreResult()

    data object FileNotFound : RestoreResult()
    data object UnhandledError : RestoreResult()
}
