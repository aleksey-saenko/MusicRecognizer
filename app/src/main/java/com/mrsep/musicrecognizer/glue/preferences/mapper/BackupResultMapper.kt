package com.mrsep.musicrecognizer.glue.preferences.mapper

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.feature.preferences.domain.BackupResult
import javax.inject.Inject

typealias BackupResultDo = com.mrsep.musicrecognizer.data.backup.BackupResult

class BackupResultMapper @Inject constructor() : Mapper<BackupResultDo, BackupResult> {

    override fun map(input: BackupResultDo): BackupResult = when (input) {
        com.mrsep.musicrecognizer.data.backup.BackupResult.Success -> BackupResult.Success
        com.mrsep.musicrecognizer.data.backup.BackupResult.FileNotFound -> BackupResult.FileNotFound
        com.mrsep.musicrecognizer.data.backup.BackupResult.UnhandledError -> BackupResult.UnhandledError
    }
}
