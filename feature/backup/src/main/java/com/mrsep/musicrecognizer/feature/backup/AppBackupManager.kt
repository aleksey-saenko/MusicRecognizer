package com.mrsep.musicrecognizer.feature.backup

import android.net.Uri
import androidx.annotation.Keep
import com.mrsep.musicrecognizer.feature.backup.data.InstantJsonSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

internal interface AppBackupManager {

    suspend fun estimateAppDataSize(): Map<BackupEntry, Long>

    suspend fun readBackupMetadata(source: Uri): BackupMetadataResult

    suspend fun backup(destination: Uri, entries: Set<BackupEntry>): BackupResult

    suspend fun restore(source: Uri, entries: Set<BackupEntry>): RestoreResult
}

@Keep
internal enum class BackupEntry { Data, Preferences }

@Serializable
internal data class BackupMetadata(
    @SerialName("backupSignature")
    val backupSignature: String,
    @SerialName("appVersionCode")
    val appVersionCode: Int,
    @SerialName("creationDate")
    @Serializable(with = InstantJsonSerializer::class)
    val creationDate: Instant,
    @SerialName("entries")
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
