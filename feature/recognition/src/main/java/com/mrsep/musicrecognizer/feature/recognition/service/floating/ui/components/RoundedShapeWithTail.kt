package com.mrsep.musicrecognizer.feature.recognition.service.floating.ui.components

import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.min

internal enum class RoundedShapeTailSide {
    Left,
    Right
}

@Immutable
internal data class RoundedShapeWithTail(
    val topLeftCornerRadius: Dp,
    val topRightCornerRadius: Dp,
    val bottomRightCornerRadius: Dp,
    val bottomLeftCornerRadius: Dp,
    val tailSide: RoundedShapeTailSide = RoundedShapeTailSide.Right,
    val tailDepth: Dp = 10.dp,
    val tailHeight: Dp = 14.dp,
    val tailTipRadius: Dp = 2.dp,
    val tailCenterYFraction: Float = 0.5f, // 0f = top, 0.5f = center, 1f = bottom
    val tailCenterYOffset: Dp = 0.dp, // Extra vertical offset
) : Shape {

    init {
        require(tailDepth > 0.dp) { "tailDepth must be > 0.dp" }
        require(tailHeight > 0.dp) { "tailHeight must be > 0.dp" }
    }

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val topLeftPx = with(density) { topLeftCornerRadius.toPx() }
        val topRightPx = with(density) { topRightCornerRadius.toPx() }
        val bottomRightPx = with(density) { bottomRightCornerRadius.toPx() }
        val bottomLeftPx = with(density) { bottomLeftCornerRadius.toPx() }

        val tailDepthPx = with(density) { tailDepth.toPx() }
        val tailHeightPx = with(density) { tailHeight.toPx() }
        val tailTipRadiusPx = with(density) { tailTipRadius.toPx() }
        val tailOffsetYPx = with(density) { tailCenterYOffset.toPx() }

        val w = size.width
        val h = size.height

        // Keep the body valid even if the tail is larger than the available width
        val safeTailDepthPx = min(tailDepthPx, w)

        val bodyLeft = if (tailSide == RoundedShapeTailSide.Left) safeTailDepthPx else 0f
        val bodyRight = if (tailSide == RoundedShapeTailSide.Right) w - safeTailDepthPx else w

        val bodyWidth = (bodyRight - bodyLeft).coerceAtLeast(0f)

        // Clamp each corner radius to the available body size
        val maxCornerRadius = min(bodyWidth / 2f, h / 2f)
        val clampedTopLeftPx = min(topLeftPx, maxCornerRadius)
        val clampedTopRightPx = min(topRightPx, maxCornerRadius)
        val clampedBottomRightPx = min(bottomRightPx, maxCornerRadius)
        val clampedBottomLeftPx = min(bottomLeftPx, maxCornerRadius)

        // The tail must fit between the rounded corners on the tail side
        val (sideTopCornerRadius, sideBottomCornerRadius) = when (tailSide) {
            RoundedShapeTailSide.Left -> clampedTopLeftPx to clampedBottomLeftPx
            RoundedShapeTailSide.Right -> clampedTopRightPx to clampedBottomRightPx
        }

        val availableStraightHeight = (h - sideTopCornerRadius - sideBottomCornerRadius).coerceAtLeast(0f)
        val halfTailHeight = min(tailHeightPx / 2f, availableStraightHeight / 2f)

        // Prevent the tail tip rounding from breaking the geometry if the tip radius is too large
        val safeTailTipRadiusPx = min(tailTipRadiusPx, min(halfTailHeight, safeTailDepthPx))

        val desiredCenterY = h * tailCenterYFraction + tailOffsetYPx
        val minCenterY = sideTopCornerRadius + halfTailHeight
        val maxCenterY = h - sideBottomCornerRadius - halfTailHeight

        val centerY = if (minCenterY <= maxCenterY) {
            desiredCenterY.coerceIn(minCenterY, maxCenterY)
        } else {
            h / 2f
        }

        // Draw a single continuous path clockwise starting from the top-left corner
        val path = Path()
        path.moveTo(bodyLeft, clampedTopLeftPx)

        // 1. Top-Left Corner
        if (clampedTopLeftPx > 0f) {
            path.arcTo(
                rect = Rect(bodyLeft, 0f, bodyLeft + 2 * clampedTopLeftPx, 2 * clampedTopLeftPx),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
        } else {
            path.lineTo(bodyLeft, 0f)
        }

        // 2. Top-Right Corner
        if (clampedTopRightPx > 0f) {
            path.arcTo(
                rect = Rect(bodyRight - 2 * clampedTopRightPx, 0f, bodyRight, 2 * clampedTopRightPx),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
        } else {
            path.lineTo(bodyRight, 0f)
        }

        // 3. Right Edge and Tail (if right-sided)
        if (tailSide == RoundedShapeTailSide.Right) {
            // Draw down to the top of the tail
            path.lineTo(bodyRight, centerY - halfTailHeight)
            // Slant outwards to the tip
            path.lineTo(w - safeTailTipRadiusPx, centerY - safeTailTipRadiusPx)
            // Round the tip
            path.quadraticTo(
                w, centerY,
                w - safeTailTipRadiusPx, centerY + safeTailTipRadiusPx
            )
            // Slant inwards back to the body
            path.lineTo(bodyRight, centerY + halfTailHeight)
        }

        // 4. Bottom-Right Corner
        if (clampedBottomRightPx > 0f) {
            path.arcTo(
                rect = Rect(bodyRight - 2 * clampedBottomRightPx, h - 2 * clampedBottomRightPx, bodyRight, h),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
        } else {
            path.lineTo(bodyRight, h)
        }

        // 5. Bottom-Left Corner
        if (clampedBottomLeftPx > 0f) {
            path.arcTo(
                rect = Rect(bodyLeft, h - 2 * clampedBottomLeftPx, bodyLeft + 2 * clampedBottomLeftPx, h),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
        } else {
            path.lineTo(bodyLeft, h)
        }

        // 6. Left Edge and Tail (if left-sided)
        if (tailSide == RoundedShapeTailSide.Left) {
            // Draw up to the bottom of the tail
            path.lineTo(bodyLeft, centerY + halfTailHeight)
            // Slant outwards to the tip
            path.lineTo(safeTailTipRadiusPx, centerY + safeTailTipRadiusPx)
            // Round the tip
            path.quadraticTo(
                0f, centerY,
                safeTailTipRadiusPx, centerY - safeTailTipRadiusPx
            )
            // Slant inwards back to the body
            path.lineTo(bodyLeft, centerY - halfTailHeight)
        }

        // 7. Close the path (draws the remaining line back to the start of the top-left corner)
        path.close()

        return Outline.Generic(path)
    }
}

@Immutable
internal data class SegmentedTailShapes(
    val commonShape: Shape,
    val leftButtonShape: Shape,
    val rightButtonShape: Shape
)

@Composable
internal fun rememberSegmentedTailShapes(
    isLeftAnchored: Boolean,
    cornerRadius: Dp = 16.dp,
    tailDepth: Dp = 10.dp,
    tailHeight: Dp = 14.dp,
    tailTipRadius: Dp = 2.dp,
    tailCenterYFraction: Float = 0.5f,
    tailCenterYOffset: Dp = 0.dp,
): SegmentedTailShapes {
    val tailSide = if (isLeftAnchored) {
        RoundedShapeTailSide.Left
    } else {
        RoundedShapeTailSide.Right
    }

    return remember(
        isLeftAnchored,
        cornerRadius,
        tailDepth,
        tailHeight,
        tailTipRadius,
        tailCenterYFraction,
        tailCenterYOffset
    ) {
        val commonShape: Shape = RoundedShapeWithTail(
            topLeftCornerRadius = cornerRadius,
            topRightCornerRadius = cornerRadius,
            bottomRightCornerRadius = cornerRadius,
            bottomLeftCornerRadius = cornerRadius,
            tailSide = tailSide,
            tailDepth = tailDepth,
            tailHeight = tailHeight,
            tailTipRadius = tailTipRadius,
            tailCenterYFraction = tailCenterYFraction,
            tailCenterYOffset = tailCenterYOffset,
        )

        val leftButtonShape: Shape = if (tailSide == RoundedShapeTailSide.Left) {
            RoundedShapeWithTail(
                topLeftCornerRadius = cornerRadius,
                topRightCornerRadius = 0.dp,
                bottomRightCornerRadius = 0.dp,
                bottomLeftCornerRadius = cornerRadius,
                tailSide = RoundedShapeTailSide.Left,
                tailDepth = tailDepth,
                tailHeight = tailHeight,
                tailTipRadius = tailTipRadius,
                tailCenterYFraction = tailCenterYFraction,
                tailCenterYOffset = tailCenterYOffset,
            )
        } else {
            AbsoluteRoundedCornerShape(
                topLeft = cornerRadius,
                topRight = 0.dp,
                bottomRight = 0.dp,
                bottomLeft = cornerRadius
            )
        }

        val rightButtonShape: Shape = if (tailSide == RoundedShapeTailSide.Right) {
            RoundedShapeWithTail(
                topLeftCornerRadius = 0.dp,
                topRightCornerRadius = cornerRadius,
                bottomRightCornerRadius = cornerRadius,
                bottomLeftCornerRadius = 0.dp,
                tailSide = RoundedShapeTailSide.Right,
                tailDepth = tailDepth,
                tailHeight = tailHeight,
                tailTipRadius = tailTipRadius,
                tailCenterYFraction = tailCenterYFraction,
                tailCenterYOffset = tailCenterYOffset,
            )
        } else {
            AbsoluteRoundedCornerShape(
                topLeft = 0.dp,
                topRight = cornerRadius,
                bottomRight = cornerRadius,
                bottomLeft = 0.dp
            )
        }

        SegmentedTailShapes(
            commonShape = commonShape,
            leftButtonShape = leftButtonShape,
            rightButtonShape = rightButtonShape
        )
    }
}
