package com.mrsep.musicrecognizer.core.data.enqueued

import com.mrsep.musicrecognizer.core.domain.recognition.AudioSample
import java.io.File
import java.util.zip.ZipInputStream

interface AudioSampleDataSource {

    fun getFiles(): Array<File>

    fun getTotalSize(): Long

    suspend fun copy(sample: AudioSample): AudioSample?

    suspend fun import(inputStream: ZipInputStream, filename: String): File?

    suspend fun read(file: File): AudioSample?

    suspend fun delete(file: File): Boolean

    suspend fun deleteAll(): Boolean
}
