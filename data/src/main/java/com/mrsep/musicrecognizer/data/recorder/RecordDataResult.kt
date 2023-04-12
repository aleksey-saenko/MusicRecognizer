package com.mrsep.musicrecognizer.data.recorder

import java.io.File

sealed class RecordDataResult {

    data class Success(val file: File) : RecordDataResult()
    data class Error(val throwable: Throwable) : RecordDataResult()

}