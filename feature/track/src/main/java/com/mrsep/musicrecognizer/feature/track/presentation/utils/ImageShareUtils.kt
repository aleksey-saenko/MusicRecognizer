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
import java.io.File
import com.mrsep.musicrecognizer.core.strings.R as StringsR

internal object ImageShareUtils {

    private const val JPEG_QUALITY = 90

    suspend fun getImageFileForSharing(
        context: Context,
        imageUrl: String,
        fileName: String,
        fileNameFallback: String,
    ): Uri? = withContext(Dispatchers.IO) {
        val imageLoader = context.imageLoader
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .size(Size.ORIGINAL)
            .build()
        val bitmap = imageLoader.execute(request).image
            ?.toBitmap()
            ?: return@withContext null
        val file = createImageFileToShare(context, bitmap, fileName, fileNameFallback)
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

    private fun createImageFileToShare(
        context: Context,
        bitmap: Bitmap,
        imageFileName: String,
        imageFileNameFallback: String,
    ): File? = try {
        val shareFolder = File(context.cacheDir, "share").apply {
            deleteRecursively()
            mkdirs()
        }
        val filename = trimAndCleanFileName(imageFileName) ?: imageFileNameFallback
        val fileToShare = shareFolder.resolve("$filename.jpg")
        fileToShare.outputStream().buffered().use { outputStream ->
            check(bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream))
        }
        fileToShare
    } catch (e: Exception) {
        Log.e(this::class.simpleName, "Failed to createImageFileToShare", e)
        null
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
