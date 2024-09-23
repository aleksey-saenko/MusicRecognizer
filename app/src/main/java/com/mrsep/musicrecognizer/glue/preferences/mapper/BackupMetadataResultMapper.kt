package com.mrsep.musicrecognizer.glue.preferences.mapper

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.feature.preferences.domain.BackupEntry
import com.mrsep.musicrecognizer.feature.preferences.domain.BackupMetadataResult
import javax.inject.Inject

typealias BackupMetadataResultDo = com.mrsep.musicrecognizer.data.backup.BackupMetadataResult

class BackupMetadataResultMapper @Inject constructor(
    private val entryMapper: BidirectionalMapper<BackupEntryDo, BackupEntry>,
): Mapper<BackupMetadataResultDo, BackupMetadataResult> {

    override fun map(input: BackupMetadataResultDo): BackupMetadataResult = when (input) {
        is com.mrsep.musicrecognizer.data.backup.BackupMetadataResult.Success -> BackupMetadataResult.Success(
            appVersionCode = input.metadata.appVersionCode,
            creationDate = input.metadata.creationDate,
            entryUncompressedSize = input.entryUncompressedSize.mapKeys { (entry, _) -> entryMapper.map(entry) },
        )
        com.mrsep.musicrecognizer.data.backup.BackupMetadataResult.FileNotFound -> BackupMetadataResult.FileNotFound
        com.mrsep.musicrecognizer.data.backup.BackupMetadataResult.MalformedBackup -> BackupMetadataResult.MalformedBackup
        com.mrsep.musicrecognizer.data.backup.BackupMetadataResult.NotBackupFile -> BackupMetadataResult.NotBackupFile
        com.mrsep.musicrecognizer.data.backup.BackupMetadataResult.UnhandledError -> BackupMetadataResult.UnhandledError
    }
}