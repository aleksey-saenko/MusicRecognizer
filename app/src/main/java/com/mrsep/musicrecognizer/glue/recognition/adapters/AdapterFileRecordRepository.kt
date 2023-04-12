package com.mrsep.musicrecognizer.glue.recognition.adapters

import com.mrsep.musicrecognizer.data.recorder.FileRecordDataRepository
import com.mrsep.musicrecognizer.feature.recognition.domain.FileRecordRepository
import java.io.File
import javax.inject.Inject

class AdapterFileRecordRepository @Inject constructor(
    private val fileRecordDataRepository: FileRecordDataRepository
) : FileRecordRepository {

    override fun getFileForNewRecord(extension: String): File {
        return fileRecordDataRepository.getFileForNewRecord(extension)
    }

    override fun delete(recordFile: File) {
        fileRecordDataRepository.delete(recordFile)
    }
}