package com.mrsep.musicrecognizer.glue.recognitionqueue.adapters

import com.mrsep.musicrecognizer.data.recorder.FileRecordDataRepository
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.FileRecordRepository
import java.io.File
import javax.inject.Inject

class AdapterFileRecordRepository @Inject constructor(
    private val fileRecordDataRepository: FileRecordDataRepository
) : FileRecordRepository {

    override fun delete(recordFile: File) {
        fileRecordDataRepository.delete(recordFile)
    }

}