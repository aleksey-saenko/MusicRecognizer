package com.mrsep.musicrecognizer.data.enqueued

import android.content.Context
import android.util.Log
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import javax.inject.Inject

internal class RecordingFileDataSourceImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : RecordingFileDataSource {

    private val recordsDirPath = "${appContext.filesDir.absolutePath}/enqueued_records"

    init {
        File(recordsDirPath).run { if (!exists()) mkdir() }
    }

    override suspend fun write(recording: ByteArray): File? {
        return try {
            withContext(ioDispatcher) {
                val resultFile = File("$recordsDirPath/rec_${System.currentTimeMillis()}")
                resultFile.outputStream().buffered().use { stream ->
                    stream.write(recording)
                    resultFile
                }
            }
        } catch (e: IOException) {
            Log.e(this::class.simpleName, "Failed to write recording file", e)
            null
        }
    }

    override suspend fun read(file: File): ByteArray? {
        return try {
            withContext(ioDispatcher) {
                file.inputStream().buffered().use { stream ->
                    stream.readBytes()
                }
            }
        } catch (e: IOException) {
            Log.e(this::class.simpleName, "Failed to read recording file", e)
            null
        }
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
            File(recordsDirPath).listFiles()?.forEach { file ->
                if (!delete(file)) allDeleted = false
            }
            allDeleted
        }
    }
}
