package com.mrsep.musicrecognizer.feature.recognition.widget.util

import android.content.Context
import android.graphics.Paint
import android.util.TypedValue
import android.widget.TextView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.ui.util.pxToDp
import com.mrsep.musicrecognizer.core.ui.util.spToPx

internal object FontUtils {

    fun measureTextViewHeight(
        context: Context,
        fontSize: TextUnit,
        lines: Int,
        includeFontPadding: Boolean
    ): Int {
        require(fontSize.isSp) { "Font size must be sp" }
        val textView = TextView(context).apply {
            this.includeFontPadding = includeFontPadding
            this.minLines = lines
            this.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.value)
        }
        textView.measure(0, 0)
        return textView.measuredHeight
    }

    fun measureTextExtraPaddings(
        context: Context,
        fontSize: TextUnit,
    ): Pair<Dp, Dp> = with(context) {
        require(fontSize.isSp) { "Font size must be sp" }
        return Paint().run {
            this.textSize = spToPx(fontSize.value)
            val fm = getFontMetrics()
            val extraPaddingTop = pxToDp(fm.ascent - fm.top).dp
            val extraPaddingBottom = pxToDp(fm.bottom - fm.descent).dp
            extraPaddingTop to extraPaddingBottom
        }
    }
}
