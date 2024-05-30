package com.mrsep.musicrecognizer.feature.recognition.widget.util

import android.content.Context
import android.util.TypedValue
import android.widget.TextView
import androidx.compose.ui.unit.TextUnit

internal object FontUtils {

    fun measureTextViewHeight(
        context: Context,
        fontSize: TextUnit,
        lines: Int
    ): Int {
        if (!fontSize.isSp) throw IllegalArgumentException("Font size must be sp")
        val titleTextView = TextView(context).apply {
            this.minLines = lines
            this.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.value)

        }
        titleTextView.measure(0, 0)
        return titleTextView.measuredHeight
    }
}