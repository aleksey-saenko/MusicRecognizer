package com.mrsep.musicrecognizer.core.network

import android.content.Context
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.nio.file.Files

internal class HttpFileLoggingInterceptor(
    private val appContext: Context,
    private val scope: CoroutineScope
) : Interceptor {

    private val rootDir by lazy {
        File(appContext.filesDir, LOG_DIR).apply { if (!exists()) mkdirs() }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        val body = response.body ?: return response
        val responseTime = response.receivedResponseAtMillis
        val host = request.url.host
        val newBody = if (body.contentType() == "application/json".toMediaType()) {
            val json = body.string()
            if (json.isNotBlank()) {
                val responseFile = File("$rootDir/${responseTime}_${host}_json.txt")
                val responseData = JSONObject(json).toString(4).encodeToByteArray()
                writeLogFile(responseFile, responseData)
            }
            json.toResponseBody(body.contentType())
        } else {
            val responseData = body.bytes()
            response.receivedResponseAtMillis
            val responseFile = File("$rootDir/${responseTime}_${host}_")
            writeLogFile(responseFile, responseData)
            responseData.toResponseBody(body.contentType())
        }

        return response.newBuilder()
            .body(newBody)
            .build()
    }

    private fun writeLogFile(logFile: File, data: ByteArray) {
        scope.launch(Dispatchers.IO) {
            try {
                Files.write(logFile.toPath(), data)
            } catch (e: IOException) {
                val message = "Error during log file writing"
                Log.e(this::class.simpleName, message, e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(appContext, message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    companion object {
        private const val LOG_DIR = "http_logger"
    }
}
