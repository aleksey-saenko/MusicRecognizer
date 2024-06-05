package com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import kotlin.math.exp
import kotlin.math.pow

/**
 * Properties used to customize the appearance of a [WaveAnimated].
 *
 * @property lineWidthFactor specifies the width of a single line in percents of
 * min composable constraints.
 * @property spaceWidthFactor specifies the width of a space between two lines in percents of
 * single line width.
 * @property periods sets the number of periods visible at the same time
 * @property animationSpeed sets animation speed in milliseconds
 * (the time it takes to pass the full width by each period)
 * @property inverseDirection set the wave direction, if true - RTL, otherwise LTR
 * @property baseColor primary color of lines
 * @property activatedColor secondary color of lines
 */
@Immutable
internal data class WaveAnimatedProperties(
    val lineWidthFactor: Float = 0.045f,
    val spaceWidthFactor: Float = 1.3f,
    val periods: Int = 2,
    val animationSpeed: Int = 2_500,
    val inverseDirection: Boolean = false,
    val baseColor: Color = Color.Blue,
    val activatedColor: Color = Color.Red
)

@Composable
internal fun WaveAnimated(
    activated: Boolean,
    amplitudeFactor: Float,
    modifier: Modifier = Modifier,
    properties: WaveAnimatedProperties = WaveAnimatedProperties()
) {
    BoxWithConstraints(
        modifier = modifier
    ) {
        val minSizePx = constraints.maxWidth.coerceAtMost(constraints.maxHeight)
        val maxWidthPx = constraints.maxWidth
        val maxHeightPx = constraints.maxHeight
        val lineWidthPx = (minSizePx * properties.lineWidthFactor).toInt()
        val spaceWidthPx = (lineWidthPx * properties.spaceWidthFactor).toInt()
        val lineSpaceWidthPx = lineWidthPx + spaceWidthPx
        val linesCount = (maxWidthPx - spaceWidthPx) / lineSpaceWidthPx
        val spaceCount = linesCount - 1
        val lineLengthPx = maxHeightPx - lineWidthPx // * 0.5f

        val currentColor by animateColorAsState(
            targetValue = if (activated) properties.activatedColor else properties.baseColor,
            animationSpec = tween(durationMillis = 50),
            label = "currentColor"
        )
        val smoothAmplFactor by animateFloatAsState(
            targetValue = when (amplitudeFactor) {
                0f -> 0.25f
                in 0.1f..0.3f -> 0.5f
                in 0.3f..0.5f -> 0.75f
                else -> 1f
            },
            animationSpec = tween(easing = EaseInQuart, durationMillis = 500),
            label = "smoothAmplFactor"
        )
        val infiniteTransition = rememberInfiniteTransition(label = "lineLengths")
        val animatables = List(linesCount) { index ->
            infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(properties.animationSpeed, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(
                        offsetMillis = properties.animationSpeed / (linesCount - 1) *
                            index * properties.periods,
                        offsetType = StartOffsetType.FastForward
                    )
                ),
                label = "line#${index}Length"
            )
        }
        val gaussianFactors = remember(linesCount) { generateGaussianList(linesCount) }

        val startXOffset = lineWidthPx / 2 +
            (maxWidthPx - linesCount * lineWidthPx - spaceCount * spaceWidthPx) / 2
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerY = this.size.height / 2
            animatables.map { lineLengthPx * it.value }.forEachIndexed { index, thisLineLength ->
                val xOffset = if (properties.inverseDirection) {
                    startXOffset + lineSpaceWidthPx * index * 1f
                } else {
                    maxWidthPx - startXOffset - lineSpaceWidthPx * index * 1f
                }
                val calcHalfLength = (thisLineLength / 2) * gaussianFactors[index] * smoothAmplFactor
                drawLine(
                    color = currentColor,
                    start = Offset(x = xOffset, y = centerY + calcHalfLength),
                    end = Offset(x = xOffset, y = centerY - calcHalfLength),
                    strokeWidth = lineWidthPx.toFloat(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

private fun generateGaussianList(size: Int): List<Float> {
    val mean = (size - 1) / 2.0
    val stdDev = size / 4.5
    val gaussianList = mutableListOf<Float>()
    for (i in 0 until size) {
        val x = ((i - mean) / stdDev).pow(2.0)
        val y = exp(-x / 2.0)
        gaussianList.add(y.toFloat())
    }
    return gaussianList
}
