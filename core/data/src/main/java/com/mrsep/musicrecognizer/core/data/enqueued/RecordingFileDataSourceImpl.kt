package com.mrsep.musicrecognizer.core.data.enqueued

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.core.domain.recognition.AudioRecording
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.time.Instant
import java.util.zip.ZipInputStream
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

internal class RecordingFileDataSourceImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : RecordingFileDataSource {

    private val recordingsDir = appContext.filesDir.resolve("enqueued_records/")

    init {
        recordingsDir.run { if (!exists()) mkdir() }
    }

    override fun getFiles(): Array<File> {
        return requireNotNull(recordingsDir.listFiles())
    }

    override fun getTotalSize(): Long {
        return getFiles().fold(0L) { acc, file -> acc + file.length() }
    }

    override suspend fun write(recording: ByteArray, timestamp: Instant): File? {
        val recordingName = getNewRecordingName(timestamp)
        return try {
            withContext(ioDispatcher) {
                val resultFile = recordingsDir.resolve(recordingName)
                val resultPath = resultFile.toPath()
                Files.deleteIfExists(resultPath)
                Files.write(resultPath, recording, StandardOpenOption.CREATE)
                resultFile
            }
        } catch (e: IOException) {
            Log.e(this::class.simpleName, "Failed to write recording ($recordingName)", e)
            null
        }
    }

    override suspend fun import(inputStream: ZipInputStream, recordingName: String): File? {
        return try {
            withContext(ioDispatcher) {
                val resultFile = recordingsDir.resolve(recordingName)
                Files.copy(inputStream, resultFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                resultFile
            }
        } catch (e: IOException) {
            Log.e(this::class.simpleName, "Failed to import recording ($recordingName)", e)
            null
        }
    }

    override suspend fun read(file: File): AudioRecording? {
        return withContext(ioDispatcher) {
            try {
                val timestamp = Instant.ofEpochMilli(file.name.takeLastWhile(Char::isDigit).toLong())
                val duration = parseRecordingDuration(file)
                AudioRecording(
                    data = file.readBytes(),
                    duration = duration,
                    nonSilenceDuration = duration,
                    startTimestamp = timestamp,
                    isFallback = false
                )
            } catch (e: Exception) {
                Log.e(this::class.simpleName, "Failed to read recording file", e)
                null
            }
        }
    }

    @Throws(Exception::class)
    private fun parseRecordingDuration(file: File): Duration {
        val mediaExtractor = MediaExtractor().apply {
            setDataSource(file.absolutePath)
        }
        val mediaFormat = mediaExtractor.getTrackFormat(0)
        val durationUs = mediaFormat.getLong(MediaFormat.KEY_DURATION)
        return ((durationUs + 500) / 1000).milliseconds
    }

    override suspend fun delete(file: File): Boolean {
        return withContext(ioDispatcher) {
            var numTries = 0
            val maxTries = 3
            while (numTries < maxTries) {
                try {
                    if (file.exists() && file.delete()) return@withContext true
                } catch (e: Exception) {
                    Log.e(this::class.simpleName, "Failed to delete recording file ${file.path}", e)
                }
                numTries++
                delay(500L)
            }
            false
        }
    }

    override suspend fun deleteAll(): Boolean {
        return withContext(ioDispatcher) {
            var allDeleted = true
            recordingsDir.listFiles()?.forEach { file ->
                if (!delete(file)) allDeleted = false
            }
            allDeleted
        }
    }

    private fun getNewRecordingName(timestamp: Instant) = "rec_${timestamp.toEpochMilli()}"
}
