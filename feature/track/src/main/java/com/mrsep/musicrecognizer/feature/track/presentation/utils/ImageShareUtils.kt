package com.mrsep.musicrecognizer.feature.track.presentation.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.size.Size
import coil3.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import com.mrsep.musicrecognizer.core.strings.R as StringsR

internal object ImageShareUtils {

    private const val JPEG_QUALITY = 90

    suspend fun getImageFileForSharing(
        context: Context,
        imageUrl: String,
        fileName: String,
        fileNameFallback: String,
    ): Uri? {
        return withContext(Dispatchers.IO) {
            val imageLoader = context.imageLoader
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .size(Size.ORIGINAL)
                .build()
            val bitmap = imageLoader.execute(request).image
                ?.toBitmap()
                ?: return@withContext null
            val file = processBitmapForSharing(context, bitmap, fileName, fileNameFallback)
                ?: return@withContext null
            FileProvider.getUriForFile(
                context,
                context.getString(
                    StringsR.string.format_file_provider_authority,
                    context.packageName
                ),
                file
            )
        }
    }

    private fun processBitmapForSharing(
        context: Context,
        bmp: Bitmap,
        imageFileName: String,
        imageFileNameFallback: String,
    ): File? {
        val shareFolder = getClearShareFolder(context) ?: return null
        shareFolder.mkdirs()
        val bytes = compressBmpToJpg(bmp)
        val filename = trimAndCleanFileName(imageFileName) ?: imageFileNameFallback
        return writeToFile(bytes, File(shareFolder, "$filename.jpg"))
    }

    private fun getClearShareFolder(context: Context): File? {
        return try {
            File(context.cacheDir, "share").also {
                it.deleteRecursively()
            }
        } catch (e: Exception) {
            Log.e(this::class.simpleName, "getClearShareFolder failed", e)
            null
        }
    }

    private fun compressBmpToJpg(bitmap: Bitmap): ByteArrayOutputStream {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, bytes)
        return bytes
    }

    private fun writeToFile(bytes: ByteArrayOutputStream, destinationFile: File): File {
        val fo = destinationFile.outputStream()
        return try {
            fo.write(bytes.toByteArray())
            destinationFile
        } finally {
            fo.flush()
            fo.close()
        }
    }

    private fun trimAndCleanFileName(fileName: String): String? {
        val trimmed = fileName.trim().take(64)
        if (trimmed.isEmpty() || trimmed == "." || trimmed == "..") return null
        return buildString(trimmed.length) {
            trimmed.forEach { char ->
                append(if (isValidFilenameChar(char)) char else '_')
            }
        }
    }

    private fun isValidFilenameChar(c: Char): Boolean {
        if ((c.code in 0x00..0x1f)) {
            return false
        }
        return when (c) {
            '"', '*', '/', ':', '<', '>', '?', '\\', '|', Char(0x7F) -> false
            else -> true
        }
    }
}
