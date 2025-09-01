package com.mrsep.musicrecognizer.core.network

import android.content.Context
import android.util.Log
import android.widget.Toast
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.time.Instant
import kotlin.time.Duration.Companion.days

private const val TAG = "HttpFileLogger"

internal class HttpFileLogger(
    private val appContext: Context,
    private val scope: CoroutineScope,
    private val daysToKeep: Int = 3,
) {

    private val rootDir by lazy {
        File(appContext.filesDir, LOG_DIR).apply { if (!exists()) mkdirs() }
    }

    init {
        scope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    deleteObsoleteFiles()
                } catch (t: Throwable) {
                    Log.e(TAG, "Error during scheduled log cleanup", t)
                }
                delay(1.days)
            }
        }
    }

    fun log(message: String) {
        val responseTime = Instant.now().toEpochMilli()
        val (type, host) = parseTypeAndHost(message)
        val filename = "${responseTime}_${type}_${host}".sanitizeForFilename()
        val logFile = File("$rootDir/${filename}.txt")
        writeLogFile(logFile, message)
    }

    fun parseTypeAndHost(message: String): Pair<String, String> {
        val type = when {
            message.startsWith("REQUEST") -> "request"
            message.startsWith("RESPONSE") -> "response"
            else -> error("Unknown message")
        }
        val url = when (type) {
            "request" -> {
                message.lineSequence().first().substringAfter(":").trim()
            }
            "response" -> {
                message.lineSequence().first { it.startsWith("FROM:") }.substringAfter(":").trim()
            }
            else -> error("Unknown message")
        }
        return type to runCatching { Url(url).host }.getOrDefault("unknown")
    }

    private fun String.sanitizeForFilename() = replace(Regex("""[^A-Za-z0-9._-]"""), "_")

    private fun writeLogFile(logFile: File, message: String) = scope.launch(Dispatchers.IO) {
        try {
            logFile.bufferedWriter().use { it.write(message) }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to write to log file", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(appContext, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun deleteObsoleteFiles() {
        val files = rootDir.listFiles() ?: return
        val cutoffMillis = Instant.now().toEpochMilli() - daysToKeep.days.inWholeMilliseconds
        files.forEach { file ->
            try {
                val name = file.nameWithoutExtension
                val epochStr = name.substringBefore('_', missingDelimiterValue = "")
                val epoch = epochStr.toLongOrNull()
                if (epoch == null) {
                    Log.d(TAG, "Skipping file with unexpected name: ${file.name}")
                    return@forEach
                }
                if (epoch < cutoffMillis) {
                    val deleted = file.delete()
                    if (!deleted) {
                        Log.w(TAG, "Failed to delete obsolete log file: ${file.name}")
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error checking/deleting file: ${file.name}", e)
            }
        }
    }

    companion object {
        private const val LOG_DIR = "http_logger"
    }
}
