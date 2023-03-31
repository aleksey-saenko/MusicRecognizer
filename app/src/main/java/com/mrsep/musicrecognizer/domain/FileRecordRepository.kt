package com.mrsep.musicrecognizer.domain

import java.io.File

interface FileRecordRepository {

    fun getFileForNewRecord(extension: String): File

    fun delete(recordFile: File)

}