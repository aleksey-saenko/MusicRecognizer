package com.mrsep.musicrecognizer.core.ui.util

import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import com.mrsep.musicrecognizer.core.strings.R as StringsR

fun Context.openUrlImplicitly(url: String) {
    startActivityOrToast(
        Intent(Intent.ACTION_VIEW, Uri.parse(url)),
        getString(StringsR.string.web_browser_not_found_toast)
    )
}

fun Context.openWebSearchImplicitly(query: String) {
    startActivityOrToast(
        Intent(Intent.ACTION_WEB_SEARCH).apply { putExtra(SearchManager.QUERY, query) },
        getString(StringsR.string.web_browser_not_found_toast)
    )
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

fun Context.copyTextToClipboard(text: String) {
    val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboardManager.setPrimaryClip(ClipData.newPlainText("", text))
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        Toast.makeText(
            this,
            getString(StringsR.string.copied),
            Toast.LENGTH_SHORT
        ).show()
    }
}

private fun Context.startActivityOrToast(intent: Intent, message: String) {
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}