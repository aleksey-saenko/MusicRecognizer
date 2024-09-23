package com.mrsep.musicrecognizer.glue.preferences.mapper

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.feature.preferences.domain.RestoreResult
import javax.inject.Inject

typealias RestoreResultDo = com.mrsep.musicrecognizer.data.backup.RestoreResult

class RestoreResultMapper @Inject constructor() : Mapper<RestoreResultDo, RestoreResult> {

    override fun map(input: RestoreResultDo): RestoreResult = when (input) {
        is com.mrsep.musicrecognizer.data.backup.RestoreResult.Success -> RestoreResult.Success(input.appRestartRequired)
        com.mrsep.musicrecognizer.data.backup.RestoreResult.FileNotFound -> RestoreResult.FileNotFound
        com.mrsep.musicrecognizer.data.backup.RestoreResult.MalformedBackup -> RestoreResult.MalformedBackup
        com.mrsep.musicrecognizer.data.backup.RestoreResult.NewerVersionBackup -> RestoreResult.NewerVersionBackup
        com.mrsep.musicrecognizer.data.backup.RestoreResult.NotBackupFile -> RestoreResult.NotBackupFile
        com.mrsep.musicrecognizer.data.backup.RestoreResult.UnhandledError -> RestoreResult.UnhandledError
    }
}
