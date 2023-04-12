package com.mrsep.musicrecognizer.feature.recognition.domain

import java.io.File

interface FileRecordRepository {

    fun getFileForNewRecord(extension: String): File

    fun delete(recordFile: File)

}