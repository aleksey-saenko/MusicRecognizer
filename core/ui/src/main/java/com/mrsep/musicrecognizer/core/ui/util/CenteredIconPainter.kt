package com.mrsep.musicrecognizer.core.ui.util

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp

/**
 * Creates and returns a new [Painter] that wraps [painter], keeping it centered
 * at the [targetSize] and tinted with [tint].
 */
fun centeredIconPainter(
    painter: Painter,
    targetSize: Dp,
    tint: Color,
): Painter = CenteredIconPainter(painter, targetSize, tint)

private class CenteredIconPainter(
    private val painter: Painter,
    private val targetSize: Dp,
    tint: Color,
) : Painter() {

    // Return Unspecified to avoid stretching by the parent's ContentScale
    override val intrinsicSize: Size = Size.Unspecified

    private val colorFilter = ColorFilter.tint(tint)

    override fun DrawScope.onDraw() {
        val targetSizePx = targetSize.toPx()

        // Protect against the requested size being larger than the canvas
        val actualSizePx = minOf(targetSizePx, size.width, size.height)

        val left = (size.width - actualSizePx) / 2f
        val top = (size.height - actualSizePx) / 2f

        translate(left = left, top = top) {
            with(painter) {
                draw(
                    size = Size(actualSizePx, actualSizePx),
                    colorFilter = this@CenteredIconPainter.colorFilter
                )
            }
        }
    }
}
