package com.mrsep.musicrecognizer.core.ui.components.workshop

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOutQuart
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AmplitudeVisualizerSmooth(
    modifier: Modifier = Modifier,
    currentValue: Float,
) {
    val color by animateColorAsState(
        targetValue = when (currentValue) {
            in 0f..0.4f -> Color(0xFF50C878)
            in 0.4f..0.6f -> Color.Yellow
            in 0.6f..0.8f -> Color(0xFFFFA500)
            else -> Color.Red
        },
        animationSpec = tween(easing = EaseInOutQuart, durationMillis = 500),
        label = "IndicatorColor"
    )
    val smoothedValue by animateFloatAsState(
        targetValue = currentValue,
        animationSpec = tween(easing = EaseInOutQuart, durationMillis = 150),
        label = "SmoothedValue"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = MaterialTheme.shapes.medium)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onBackground.copy(0.5f),
                shape = MaterialTheme.shapes.medium
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(smoothedValue)
                .fillMaxHeight()
                .background(color = color),
            content = {}
        )
    }
}

@Composable
fun AmplitudeVisualizerDirect(
    modifier: Modifier = Modifier,
    currentValue: Float,
) {
    val color = when (currentValue) {
        in 0f..0.4f -> Color(0xFF50C878)
        in 0.4f..0.6f -> Color.Yellow
        in 0.6f..0.8f -> Color(0xFFFFA500)
        else -> Color.Red
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = MaterialTheme.shapes.medium)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onBackground.copy(0.5f),
                shape = MaterialTheme.shapes.medium
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(currentValue)
                .fillMaxHeight()
                .background(color = color),
            content = {}
        )
    }
}