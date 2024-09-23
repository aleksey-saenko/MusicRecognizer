package com.mrsep.musicrecognizer.glue.preferences.adapter

import android.net.Uri
import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.feature.preferences.domain.AppBackupManager
import com.mrsep.musicrecognizer.feature.preferences.domain.BackupEntry
import com.mrsep.musicrecognizer.feature.preferences.domain.BackupMetadataResult
import com.mrsep.musicrecognizer.feature.preferences.domain.BackupResult
import com.mrsep.musicrecognizer.feature.preferences.domain.RestoreResult
import com.mrsep.musicrecognizer.glue.preferences.mapper.BackupEntryDo
import com.mrsep.musicrecognizer.glue.preferences.mapper.BackupMetadataResultDo
import com.mrsep.musicrecognizer.glue.preferences.mapper.BackupResultDo
import com.mrsep.musicrecognizer.glue.preferences.mapper.RestoreResultDo
import javax.inject.Inject

typealias AppBackupManagerDo = com.mrsep.musicrecognizer.data.backup.AppBackupManager

class AdapterAppBackupManager @Inject constructor(
    private val appBackupManagerDo: AppBackupManagerDo,
    private val entryMapper: BidirectionalMapper<BackupEntryDo, BackupEntry>,
    private val backupResultMapper: Mapper<BackupResultDo, BackupResult>,
    private val restoreResultMapper: Mapper<RestoreResultDo, RestoreResult>,
    private val metadataResultMapper: Mapper<BackupMetadataResultDo, BackupMetadataResult>,
): AppBackupManager {

    override suspend fun estimateAppDataSize(): Map<BackupEntry, Long> {
        return appBackupManagerDo.estimateAppDataSize()
            .mapKeys { (entry, _) -> entryMapper.map(entry) }
    }

    override suspend fun readBackupMetadata(source: Uri): BackupMetadataResult {
        return appBackupManagerDo.readBackupMetadata(source).run(metadataResultMapper::map)
    }

    override suspend fun backup(destination: Uri, entries: Set<BackupEntry>): BackupResult {
        val entriesDo = entries.map(entryMapper::reverseMap).toSet()
        return appBackupManagerDo.backup(destination, entriesDo).run(backupResultMapper::map)
    }

    override suspend fun restore(source: Uri, entries: Set<BackupEntry>): RestoreResult {
        val entriesDo = entries.map(entryMapper::reverseMap).toSet()
        return appBackupManagerDo.restore(source, entriesDo).run(restoreResultMapper::map)
    }
}
