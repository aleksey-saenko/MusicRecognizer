package com.mrsep.musicrecognizer.core.ui.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.mrsep.musicrecognizer.core.strings.R as StringsR

// FIXME: Youtube links are broken if youtube app is not installed
// https://issuetracker.google.com/issues/243678703
/** Validate URL before try to open  */
fun Context.openUrlImplicitly(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    startActivityOrToast(intent, getString(StringsR.string.web_browser_not_found_toast))
}

fun Context.shareImageWithText(subject: String, body: String, imageUri: Uri) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/*"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
        putExtra(Intent.EXTRA_STREAM, imageUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    startActivityOrToast(
        Intent.createChooser(shareIntent, null),
        getString(StringsR.string.cannot_share_toast)
    )
}

fun Context.shareText(subject: String, body: String) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }
    startActivityOrToast(
        Intent.createChooser(shareIntent, null),
        getString(StringsR.string.cannot_share_toast)
    )
}

private fun Context.startActivityOrToast(intent: Intent, message: String) {
    if (intent.resolveActivity(packageManager) != null) {
        startActivity(intent)
    } else {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
