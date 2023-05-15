package com.mrsep.musicrecognizer.core.common.util

import android.content.Context
import android.widget.Toast
import com.mrsep.musicrecognizer.core.strings.R as StringsR

//
//import android.content.Context
//import android.content.Intent
//import android.net.Uri
//import android.util.Patterns
//import android.widget.Toast
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import java.io.File
//import java.io.FileOutputStream
//import java.text.SimpleDateFormat
//import java.util.*

fun Context.showStubToast() = Toast
    .makeText(this, getString(StringsR.string.not_implemented), Toast.LENGTH_LONG)
    .show()

///** Validate URL before try to open  */
//fun Context.openUrlImplicitly(url: String) {
//    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//    if (intent.resolveActivity(packageManager) != null) {
//        startActivity(intent)
//    } else {
//        Toast.makeText(this, "Web browser not found", Toast.LENGTH_LONG).show()
//    }
//}
//
//fun Context.shareText(subject: String, body: String) {
//    val intent = Intent(Intent.ACTION_SEND).apply {
//        type = "text/plain"
//        putExtra(Intent.EXTRA_SUBJECT, subject)
//        putExtra(Intent.EXTRA_TEXT, body)
//    }
//    val wrappedIntent = Intent.createChooser(intent, null)
//    startActivity(wrappedIntent)
//}
//
//suspend fun Context.getFileFromAssets(fileName: String): File =
//    withContext(Dispatchers.IO) {
//        File(cacheDir, fileName)
//            .also {
//                if (!it.exists()) {
//                    it.outputStream().use { cache ->
//                        assets.open(fileName).use { inputStream ->
//                            inputStream.copyTo(cache)
//                        }
//                    }
//                }
//            }
//    }
//
//suspend fun Context.logToStorage(tag: String, content: String) {
//    withContext(Dispatchers.IO) {
//        val logFile = File("${filesDir.absolutePath}/app_log.txt")
//        val currentTime = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.ENGLISH)
//            .format(Date(System.currentTimeMillis()))
//        val record = "----------------------------------------------\n" +
//                "$currentTime\n" +
//                "$tag: $content\n" +
//                "----------------------------------------------\n"
//        try {
//            FileOutputStream(logFile, true).bufferedWriter().use { writer ->
//                writer.write(record)
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            withContext(Dispatchers.Main) {
//                Toast.makeText(
//                    this@logToStorage,
//                    "logToStorage failed, see stacktrace",
//                    Toast.LENGTH_LONG
//                ).show()
//            }
//        }
//    }
//}
