package com.mrsep.musicrecognizer.presentation.screens.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay

@Composable
fun RippleAnimated(
    activated: Boolean,
    amplitudeFactor: Float,
    modifier: Modifier = Modifier,
    circlesCount: Int = 5,
    animationSpeed: Int = 5_000,
    baseColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
    activatedColor: Color = MaterialTheme.colorScheme.primary
) {
    val currentColor by animateColorAsState(
        targetValue = if (activated) activatedColor else baseColor,
        animationSpec = tween(durationMillis = 300)
    )
    val activatedFactor by animateFloatAsState(
        targetValue = if (activated) 1.25f else 1f,
        animationSpec = tween(durationMillis = 2000)
    )
    val circles = List(circlesCount) { remember { Animatable(initialValue = 0f) } }

    circles.forEachIndexed { index, animatable ->
        LaunchedEffect(Unit) {
            delay((animationSpeed.toLong() / circlesCount) * (index + 1))
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = animationSpeed,
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Restart,
                    initialStartOffset = StartOffset(
                        (animationSpeed * 0.5).toInt(),
                        offsetType = StartOffsetType.FastForward
                    )
                )
            )
        }
    }

    Canvas(
        modifier = modifier
    ) {
        circles.forEach { animatable ->
            drawCircle(
                color = currentColor,
                radius = animatable.value * size.minDimension / 2.0f * activatedFactor,
                alpha = 1 - animatable.value,
//                style = Stroke(width = 4.dp.toPx()
            )
        }
    }
}