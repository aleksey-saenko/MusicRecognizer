package com.mrsep.musicrecognizer.data.enqueued

import java.io.File

interface RecordingFileDataSource {

    suspend fun write(recording: ByteArray): File?

    suspend fun read(file: File): ByteArray?

    suspend fun delete(file: File): Boolean

    suspend fun deleteAll(): Boolean

}