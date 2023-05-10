package com.mrsep.musicrecognizer.data.enqueued

import android.content.Context
import android.util.Log
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import java.io.File
import javax.inject.Inject

private const val TAG = "RecordingFileDataSourceImpl"

class RecordingFileDataSourceImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
): RecordingFileDataSource {

    private val recordsDirPath = "${appContext.filesDir.absolutePath}/enqueued_records"

    init {
        File(recordsDirPath).run { if (!exists()) mkdir() }
    }

    override suspend fun write(recording: ByteArray): File? {
        return runCatching {
            withContext(ioDispatcher) {
                val resultFile = File("$recordsDirPath/rec_${System.currentTimeMillis()}")
                resultFile.outputStream().buffered().use { stream ->
                    stream.write(recording)
                    resultFile
                }
            }
        }.getOrNull()
    }

    override suspend fun read(file: File): ByteArray? {
        return runCatching {
            withContext(ioDispatcher) {
                file.inputStream().buffered().use { stream ->
                    stream.readBytes()
                }
            }
        }.getOrNull()
    }


    override suspend fun delete(file: File): Boolean {
        return withContext(ioDispatcher) {
            var numTries = 0
            val maxTries = 3
            while (numTries < maxTries) {
                try {
                    if (file.exists() && file.delete()) {
                        return@withContext true
                    }
                    numTries++
                    delay(500L)
                } catch (e: Exception) {
                    Log.e(TAG, "Record file deletion failed", e)
                }
            }
            false
        }
    }

}