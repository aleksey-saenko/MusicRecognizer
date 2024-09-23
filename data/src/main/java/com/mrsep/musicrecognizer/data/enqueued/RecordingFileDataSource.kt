package com.mrsep.musicrecognizer.data.enqueued

import java.io.File
import java.util.zip.ZipInputStream

interface RecordingFileDataSource {

    fun getFiles(): Array<File>

    fun getTotalSize(): Long

    suspend fun write(recording: ByteArray): File?

    suspend fun import(inputStream: ZipInputStream, recordingName: String): File?

    suspend fun read(file: File): ByteArray?

    suspend fun delete(file: File): Boolean

    suspend fun deleteAll(): Boolean
}
