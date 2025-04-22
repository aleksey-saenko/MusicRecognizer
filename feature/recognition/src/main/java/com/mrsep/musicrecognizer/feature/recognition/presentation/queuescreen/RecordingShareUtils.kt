package com.mrsep.musicrecognizer.feature.recognition.presentation.queuescreen

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.mrsep.musicrecognizer.core.strings.R
import java.io.File

internal object RecordingShareUtils {

    // TODO: temp implementation to allow sharing of distorted recording
    fun getRecordingFileForSharing(
        context: Context,
        file: File,
    ): Uri? = try {
        val shareFolder = File(context.cacheDir, "share").also {
            it.deleteRecursively()
            it.mkdirs()
        }
        val target = file.copyTo(shareFolder.resolve(file.name.plus(".aac")))
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