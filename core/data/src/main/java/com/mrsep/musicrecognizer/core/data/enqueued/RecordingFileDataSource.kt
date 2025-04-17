package com.mrsep.musicrecognizer.core.data.enqueued

import com.mrsep.musicrecognizer.core.domain.recognition.AudioRecording
import java.io.File
import java.time.Instant
import java.util.zip.ZipInputStream

interface RecordingFileDataSource {

    fun getFiles(): Array<File>

    fun getTotalSize(): Long

    suspend fun write(recording: ByteArray, timestamp: Instant): File?

    suspend fun import(inputStream: ZipInputStream, recordingName: String): File?

    suspend fun read(file: File): AudioRecording?

    suspend fun delete(file: File): Boolean

    suspend fun deleteAll(): Boolean
}
