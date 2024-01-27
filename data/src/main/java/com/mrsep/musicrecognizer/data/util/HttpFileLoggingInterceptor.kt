package com.mrsep.musicrecognizer.data.util

import android.content.Context
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

internal class HttpFileLoggingInterceptor(
    private val appContext: Context,
    private val scope: CoroutineScope
) : Interceptor {

    private val rootDir = "${appContext.filesDir.absolutePath}/http_logger/"

    init {
        try {
            File(rootDir).run { if (!exists()) mkdir() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        val body = response.body!!
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
            val responseData = body.source().readByteArray()
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
                logFile.outputStream().use { stream ->
                    stream.write(data)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        appContext,
                        "LogToStorage failed, check stacktrace",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

}