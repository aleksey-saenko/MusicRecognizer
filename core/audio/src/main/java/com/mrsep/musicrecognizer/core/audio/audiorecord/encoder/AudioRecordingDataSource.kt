package com.mrsep.musicrecognizer.core.audio.audiorecord.encoder

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AudioRecordingDataSource"

@Singleton
class AudioRecordingDataSource @Inject internal constructor(
    @ApplicationContext private val appContext: Context
) {
    private val mutex = Mutex()

    internal suspend fun createNewFile(
        sessionId: UUID,
        extension: String,
    ): Result<File> = withContext(Dispatchers.IO) {
        mutex.withLock {
            runCatching {
                val sessionDir = appContext.filesDir.resolve("${SESSION_ROOT_DIR}/$sessionId")
                sessionDir.mkdirs()
                sessionDir.resolve("${UUID.randomUUID()}.$extension").also { newFile ->
                    newFile.createNewFile()
                }
            }.onFailure { cause ->
                Log.e(TAG, "Failed to create a new audio recording file", cause)
            }
        }
    }

    internal suspend fun deleteSessionFiles(sessionId: UUID): Unit = withContext(Dispatchers.IO) {
        mutex.withLock {
            runCatching {
                val sessionDir = appContext.filesDir.resolve("${SESSION_ROOT_DIR}/$sessionId")
                sessionDir.deleteRecursively()
            }.onFailure { cause ->
                Log.e(TAG, "Failed to clear audio recording files with session $sessionId", cause)
            }
        }
    }

    suspend fun clear(): Unit = withContext(Dispatchers.IO) {
        mutex.withLock {
            runCatching {
                val rootDir = appContext.filesDir.resolve(SESSION_ROOT_DIR)
                rootDir.deleteRecursively()
            }.onFailure { cause ->
                Log.e(TAG, "Failed to clear audio recording files", cause)
            }
        }
    }

    companion object {
        private const val SESSION_ROOT_DIR = "recording_session"
    }
}