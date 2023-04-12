package com.mrsep.musicrecognizer.core.ui.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Patterns
import android.widget.Toast
import com.mrsep.musicrecognizer.core.strings.R as StringsR

fun validateUrl(potentialUrl: String) = Patterns.WEB_URL.matcher(potentialUrl).matches()
fun validUrlOrNull(potentialUrl: String) = if (validateUrl(potentialUrl)) potentialUrl else null

/** Validate URL before try to open  */
fun Context.openUrlImplicitly(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    startActivityOrToast(intent, getString(StringsR.string.web_browser_not_found_toast))
}

fun Context.shareText(subject: String, body: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }
    val wrappedIntent = Intent.createChooser(intent, null)
    startActivityOrToast(wrappedIntent, getString(StringsR.string.cannot_share_toast))
}

@SuppressLint("QueryPermissionsNeeded")
private fun Context.startActivityOrToast(intent: Intent, message: String) {
    if (intent.resolveActivity(packageManager) != null) {
        startActivity(intent)
    } else {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
