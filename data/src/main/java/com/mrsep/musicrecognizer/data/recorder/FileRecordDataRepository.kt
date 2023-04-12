package com.mrsep.musicrecognizer.data.recorder

import java.io.File

interface FileRecordDataRepository {
    fun getFileForNewRecord(extension: String): File
    fun delete(recordFile: File)
}