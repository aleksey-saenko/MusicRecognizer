package com.mrsep.musicrecognizer.glue.preferences.mapper

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.feature.preferences.domain.BackupEntry
import javax.inject.Inject

typealias BackupEntryDo = com.mrsep.musicrecognizer.data.backup.BackupEntry

class BackupEntryMapper @Inject constructor() : BidirectionalMapper<BackupEntryDo, BackupEntry> {

    override fun map(input: BackupEntryDo): BackupEntry = when (input) {
        com.mrsep.musicrecognizer.data.backup.BackupEntry.Data -> BackupEntry.Data
        com.mrsep.musicrecognizer.data.backup.BackupEntry.Preferences -> BackupEntry.Preferences
    }

    override fun reverseMap(input: BackupEntry): BackupEntryDo = when (input) {
        BackupEntry.Data -> com.mrsep.musicrecognizer.data.backup.BackupEntry.Data
        BackupEntry.Preferences -> com.mrsep.musicrecognizer.data.backup.BackupEntry.Preferences
    }
}