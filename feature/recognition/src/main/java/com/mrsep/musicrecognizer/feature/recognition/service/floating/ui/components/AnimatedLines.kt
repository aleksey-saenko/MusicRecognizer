package com.mrsep.musicrecognizer.feature.recognition.service.floating.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin

@Composable
internal fun AnimatedLines(
    isActivated: Boolean,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF000000),
    cycleDurationMs: Int = 2_000,
    minScale: Float = 0.7f,
    maxScale: Float = 1.15f,
    waveSharpness: Float = 2.5f
) {
    val activationProgress by animateFloatAsState(
        targetValue = if (isActivated) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "ActivationProgress"
    )
    val progress = remember { Animatable(0f) }
    val isAnimating = isActivated || activationProgress > 0f

    LaunchedEffect(isAnimating, cycleDurationMs) {
        if (isAnimating) {
            // Ensure the wave always starts from the first (leftmost) line
            if (isActivated) {
                progress.snapTo(0f)
            }
            progress.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = cycleDurationMs, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        }
    }

    // UiR.drawable.outline_lines_shift1_48
    val lineCentersX = remember { floatArrayOf(6.675f, 15.675f, 24.675f, 33.675f, 42.675f) }
    val lineHeights = remember { floatArrayOf(14f, 40f, 14f, 26f, 12f) }

    Canvas(modifier = modifier) {
        val scaleX = size.width / 48f
        val scaleY = size.height / 48f
        val strokeWidth = 4f * scaleX
        val centerY = 24f * scaleY
        val currentProgress = progress.value
        val phaseShift = 0.65f

        for (i in 0 until 5) {
            val x = lineCentersX[i] * scaleX
            val baseHeight = lineHeights[i] * scaleY

            val angle = (currentProgress * 2 * PI) - (i * phaseShift)
            val waveAmplitude = ((sin(angle) + 1.0) / 2.0).pow(waveSharpness.toDouble()).toFloat()
            val targetScale = minScale + waveAmplitude * (maxScale - minScale)
            val currentScale = 1f + activationProgress * (targetScale - 1f)
            val currentHeight = baseHeight * currentScale

            drawLine(
                color = color,
                start = Offset(x, centerY - currentHeight / 2f),
                end = Offset(x, centerY + currentHeight / 2f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}
