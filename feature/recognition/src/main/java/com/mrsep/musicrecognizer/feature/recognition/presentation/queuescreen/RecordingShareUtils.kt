package com.mrsep.musicrecognizer.feature.recognition.presentation.queuescreen

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.mrsep.musicrecognizer.core.strings.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

internal object RecordingShareUtils {

    // TODO: temp implementation to allow sharing of distorted recording
    suspend fun getRecordingFileForSharing(
        context: Context,
        file: File,
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val shareFolder = File(context.cacheDir, "share").apply {
                deleteRecursively()
                mkdirs()
            }
            val target = file.copyTo(shareFolder.resolve("sample.${file.extension}"))
            FileProvider.getUriForFile(
                context,
                context.getString(
                    R.string.format_file_provider_authority,
                    context.packageName
                ),
                target
            )
        } catch (e: Exception) {
            Log.e(this::class.simpleName, "Recording sharing failed", e)
            null
        }
    }
}