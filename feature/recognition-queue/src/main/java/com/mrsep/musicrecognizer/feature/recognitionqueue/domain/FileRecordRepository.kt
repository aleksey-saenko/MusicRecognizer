package com.mrsep.musicrecognizer.feature.recognitionqueue.domain

import java.io.File

interface FileRecordRepository {

    fun delete(recordFile: File)

}