package com.mrsep.musicrecognizer.feature.developermode.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOutQuart
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
internal fun AmplitudeVisualizerSmooth(
    modifier: Modifier = Modifier,
    currentValue: State<Float>,
) {
    val color = animateColorAsState(
        targetValue = when (currentValue.value) {
            in 0f..0.4f -> Color(0xFF50C878)
            in 0.4f..0.6f -> Color.Yellow
            in 0.6f..0.8f -> Color(0xFFFFA500)
            else -> Color.Red
        },
        animationSpec = tween(easing = EaseInOutQuart, durationMillis = 500),
        label = "IndicatorColor"
    )
    val smoothedValue = animateFloatAsState(
        targetValue = currentValue.value,
        animationSpec = tween(easing = EaseInOutQuart, durationMillis = 150),
        label = "SmoothedValue"
    )
    Box(
        modifier = modifier
            .clip(shape = MaterialTheme.shapes.medium)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.medium
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                color = color.value,
                size = Size(width = size.width * smoothedValue.value, height = size.height)
            )
        }
    }
}

@Composable
internal fun AmplitudeVisualizerDirect(
    modifier: Modifier = Modifier,
    currentValue: State<Float>,
) {
    Box(
        modifier = modifier
            .clip(shape = MaterialTheme.shapes.medium)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.medium
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                color = when (currentValue.value) {
                    in 0f..0.4f -> Color(0xFF50C878)
                    in 0.4f..0.6f -> Color.Yellow
                    in 0.6f..0.8f -> Color(0xFFFFA500)
                    else -> Color.Red
                },
                size = Size(width = size.width * currentValue.value, height = size.height)
            )
        }
    }
}
