package com.mrsep.musicrecognizer.feature.track.presentation.lyrics

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.domain.preferences.FontSize

@Composable
internal fun Modifier.fontSizeHorizontalSwitcher(
    enabled: Boolean,
    fontSize: FontSize,
    onChangeFontSize: (newSize: FontSize) -> Unit,
    dragAmountForChange: Dp = 100.dp
): Modifier {
    val layoutDirection = LocalLayoutDirection.current
    var offset by remember { mutableFloatStateOf(0f) }
    val currentSize by rememberUpdatedState(fontSize)
    return this.then(
        if (enabled) {
            Modifier.pointerInput(layoutDirection) {
                val dragAmountForChangePx = dragAmountForChange.toPx()
                detectHorizontalDragGestures(
                    onDragStart = { offset = 0f },
                    onDragCancel = { offset = 0f },
                    onDragEnd = { offset = 0f }
                ) { _, dragAmount ->
                    offset += when (layoutDirection) {
                        LayoutDirection.Ltr -> dragAmount
                        LayoutDirection.Rtl -> -dragAmount
                    }
                    when {
                        offset > dragAmountForChangePx -> {
                            val newSize = currentSize.getBigger()
                            if (newSize != currentSize) onChangeFontSize(newSize)
                            offset = 0f
                        }

                        offset < -dragAmountForChangePx -> {
                            val newSize = currentSize.getSmaller()
                            if (newSize != currentSize) onChangeFontSize(newSize)
                            offset = 0f
                        }
                    }
                }
            }
        } else {
            Modifier
        }
    )
}

private fun FontSize.getBigger() = FontSize.entries.getOrNull(ordinal + 1)
    ?: FontSize.entries.last()

private fun FontSize.getSmaller() = FontSize.entries.getOrNull(ordinal - 1)
    ?: FontSize.entries.first()
