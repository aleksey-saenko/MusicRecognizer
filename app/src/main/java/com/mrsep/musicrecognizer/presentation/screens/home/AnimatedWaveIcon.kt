package com.mrsep.musicrecognizer.presentation.screens.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun AnimatedWaveIcon(
    activated: Boolean,
    modifier: Modifier = Modifier,
    linesCount: Int = 9,
    lineWidth: Dp = 4.dp,
    lineLength: Dp = 80.dp,
    spaceWidth: Dp = 6.dp,
    animationSpeed: Int = 7_000,
    baseColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
    activatedColor: Color = MaterialTheme.colorScheme.primary
) {
    val currentColor by animateColorAsState(
        targetValue = if (activated) activatedColor else baseColor,
        animationSpec = tween(durationMillis = 50)
    )

    val infiniteTransition = rememberInfiniteTransition()
    val animatables = List(linesCount) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0f, //-1 for full period of sinus
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(animationSpeed, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
                initialStartOffset = StartOffset(
                    offsetMillis = (animationSpeed / linesCount * index),
                    offsetType = StartOffsetType.FastForward
                )
            )
        )
    }
    val activatedFactor by animateFloatAsState(if (activated) 1f else 0.25f)

    Canvas(
        modifier = modifier.fillMaxSize()
//            .border(width = 1.dp, color = Color.Yellow.copy(alpha = 0.3f))
    ) {
        val centerX = this.size.width / 2
        val centerY = this.size.height / 2
        val maxIndex = animatables.lastIndex
        val lineLengthPx = lineLength.toPx()

        animatables.map {
            lineLengthPx * sin(it.value * 2 * 3.14).toFloat()
        }
            .forEachIndexed { index, currentLineLength ->
                val boundFactor = when (maxIndex - index) {
                    0, maxIndex -> 0.25f
                    1, (maxIndex - 1) -> 0.5f
                    2, (maxIndex - 2) -> 0.75f
                    else -> 1f
                }
                val xOffset = centerX - (index - linesCount / 2) * (lineWidth + spaceWidth).toPx()
                val calcHalfLength = (currentLineLength / 2) * boundFactor * activatedFactor
                drawLine(
                    color = currentColor,
                    start = Offset(x = xOffset, y = centerY + calcHalfLength),
                    end = Offset(x = xOffset, y = centerY - calcHalfLength),
                    strokeWidth = lineWidth.toPx(),
                    cap = StrokeCap.Round
                )
            }
    }

}